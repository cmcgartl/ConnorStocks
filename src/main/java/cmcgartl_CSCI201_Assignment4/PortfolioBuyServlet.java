/*package cmcgartl_CSCI201_Assignment4;

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

@WebServlet("/portfolioBuy")
public class PortfolioBuyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("got to buying servlet");
    	HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }

        String username = (String) session.getAttribute("user");
        String ticker = request.getParameter("ticker");
        int quantity;
        try {
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"Invalid quantity format\"}");
            return;
        }

        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yourDatabaseName", "username", "password");
            conn.setAutoCommit(false);
         
            if (!executeBuy(conn, username, ticker, quantity)) {
                conn.rollback();
                jsonResponse.put("message", "Failed to execute purchase");
            } else {
                conn.commit();
                jsonResponse.put("message", "Purchase successful");
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            jsonResponse.put("message", "Error processing transaction: " + e.getMessage());
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

    private boolean executeBuy(Connection conn, String username, String ticker, int quantity) throws SQLException, IOException {
        System.out.println("got to executing buy");
    	String userQuery = "SELECT balance, " + ticker + " FROM userInfo WHERE username = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setString(1, username);
        ResultSet userData = userStmt.executeQuery();
        if (!userData.next()) return false; // No user data found

        double currentPrice = getStockPrice(ticker);
        double balance = userData.getDouble("balance");
        int stockQuantity = userData.getInt(ticker);
        
        double cost = quantity * currentPrice;
        if (balance < cost) return false; // Check if user has enough balance

        balance -= cost;
        stockQuantity += quantity;

        String updateQuery = "UPDATE userInfo SET balance = ?, " + ticker + " = ? WHERE username = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
        updateStmt.setDouble(1, balance);
        updateStmt.setInt(2, stockQuantity);
        updateStmt.setString(3, username);
        updateStmt.executeUpdate();
        return true;
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
}*/
