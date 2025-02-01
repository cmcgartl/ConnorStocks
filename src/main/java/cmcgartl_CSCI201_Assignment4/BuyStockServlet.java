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
import javax.servlet.http.*;
import org.json.JSONObject;

@WebServlet("/buyStock")
public class BuyStockServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("user");
        String ticker = request.getParameter("ticker");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ConnorsDataBase2", "root", "Wg600951CM");
            conn.setAutoCommit(false);

            double stockPrice = getStockPrice(ticker); // Fetch the stock price
            if (stockPrice == -1) {
                throw new Exception("Failed to fetch stock price.");
            }

            //lines 41 - 43 from chatgpt: "How should I properly access the userInfo table for the mySQL table provided?" accessed: 4/22/24
            stmt = conn.prepareStatement("SELECT balance, " + ticker + " FROM userInfo WHERE username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int currentBalance = rs.getInt("balance");
                double totalCost = quantity * stockPrice;

                if (currentBalance >= totalCost) {
                    double newBalance = currentBalance - totalCost;
                    int newStockCount = rs.getInt(ticker) + quantity;

                   
                    stmt = conn.prepareStatement("UPDATE userInfo SET balance = ?, " + ticker + " = ? WHERE username = ?");
                    stmt.setDouble(1, newBalance);
                    stmt.setInt(2, newStockCount);
                    stmt.setString(3, username);
                    stmt.executeUpdate();

                    conn.commit();
                    String successMessage = "Bought " + quantity + " shares of " + ticker + " for $" + totalCost + " Current balance is " + newBalance;
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": true, \"message\": \"" + successMessage + "\"}");
                } else {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": false, \"message\": \"Insufficient balance.\"}");
                }
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"User not found.\"}");
            }
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Error processing transaction: " + e.getMessage() + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
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