package cmcgartl_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet("/tradeStock")
public class PortfolioTradeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }
        
        String username = (String) session.getAttribute("user");
        String ticker = request.getParameter("ticker");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String action = request.getParameter("action"); // "buy" or "sell"
        
        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ConnorsDataBase2", "root", "Wg600951CM");
            conn.setAutoCommit(false);
         
            if (!executeTrade(conn, username, ticker, quantity, action)) {
                conn.rollback();
                jsonResponse.put("message", "Failed to execute trade");
            } else {
                conn.commit();
                jsonResponse.put("message", "Trade successful");
            }
            response.setStatus(HttpServletResponse.SC_OK); 
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            //Json Response for lines 58-60, 65-67 and 40 from chatGPT "How does the Json object properly get transferred to the client side" accessed 4/23/24
            jsonResponse.put("message", "Error processing transaction");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        }
    }

    private boolean executeTrade(Connection conn, String username, String ticker, int quantity, String action) throws SQLException, IOException {
     
        String userQuery = "SELECT balance, " + ticker + " FROM userInfo WHERE username = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setString(1, username);
        ResultSet userData = userStmt.executeQuery();
        if (!userData.next()) {
        	System.out.println("NO USER FOUND FOR TRADE");
        	return false; 
        }

        double currentPrice = getStockPrice(ticker);
        double balance = userData.getDouble("balance");
        int stockQuantity = userData.getInt(ticker);
        System.out.println("CURRENT ACTION: " + action);
        
        if (action.equals("buy")) {
        	System.out.println("currently buying");
            double cost = quantity * currentPrice;
            if (balance < cost) {
            	System.out.println("INSUFFICIENT BALANCE");
            	return false; 
            }

            balance -= cost;
            stockQuantity += quantity;
        } else if (action.equals("sell")) {
        	System.out.println("selling");
            if (stockQuantity < quantity) {
            	System.out.println("NOT ENOUGH STOCKS TO SELL");
            	return false; 
            }

            double earnings = quantity * currentPrice;
            balance += earnings;
            stockQuantity -= quantity;
        } else {
        	System.out.println("INVALID ACTION");
            return false; 
        }

        String updateQuery = "UPDATE userInfo SET balance = ?, " + ticker + " = ? WHERE username = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
        updateStmt.setDouble(1, balance);
        updateStmt.setInt(2, stockQuantity);
        updateStmt.setString(3, username);
        return updateStmt.executeUpdate() > 0; 
    }

    private double getStockPrice(String ticker) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            String apiKey = "csrv5m9r01qj3u0ouflgcsrv5m9r01qj3u0oufm0";
            URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            String responseStr = response.toString();
            String keyToFind = "\"c\":";
            int startIndex = responseStr.indexOf(keyToFind) + keyToFind.length();
            int endIndex = responseStr.indexOf(",", startIndex);
            String priceStr = responseStr.substring(startIndex, endIndex).trim();
            return Double.parseDouble(priceStr);
        } finally {
            if (in != null) in.close();
            if (connection != null) connection.disconnect();
        }
    }
}
