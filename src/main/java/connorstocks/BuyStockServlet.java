package connorstocks;

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

/**
 * Servlet that processes stock purchase requests.
 * Expects a logged-in user and a POST request containing the stock ticker and quantity.
 * Retrieves real-time pricing, validates funds, and updates the user's balance and holdings.
 */

@WebServlet("/buyStock")
public class BuyStockServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Handles POST requests to buy shares of a stock.
     * Responds with a JSON success or failure message.
     */

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";
        		
    	// Retrieve the current user's session and extract the username
    	HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("user");
        
        // Extract ticker symbol and quantity from request parameters
        String ticker = request.getParameter("ticker");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
   
        	// Connect to MySQL database
        	conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            conn.setAutoCommit(false); // Enable transaction management
            
            // Fetch the current stock price from Finnhub API
            double stockPrice = getStockPrice(ticker);
            if (stockPrice == -1) {
                throw new Exception("Failed to fetch stock price.");
            }

            // Retrieve user's balance and current share count for the specified stock
            stmt = conn.prepareStatement("SELECT balance, " + ticker + " FROM userInfo WHERE username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int currentBalance = rs.getInt("balance");
                double totalCost = quantity * stockPrice;

                // Check if user has enough funds to complete the purchase
                if (currentBalance >= totalCost) {
                	
                	// Update user's balance and stock count
                    double newBalance = currentBalance - totalCost;
                    int newStockCount = rs.getInt(ticker) + quantity;

                   
                    stmt = conn.prepareStatement("UPDATE userInfo SET balance = ?, " + ticker + " = ? WHERE username = ?");
                    stmt.setDouble(1, newBalance);
                    stmt.setInt(2, newStockCount);
                    stmt.setString(3, username);
                    stmt.executeUpdate();

                    // Commit the transaction
                    conn.commit();
                    
                    // Respond with a success message
                    String successMessage = "Bought " + quantity + " shares of " + ticker + " for $" + totalCost + " Current balance is " + newBalance;
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": true, \"message\": \"" + successMessage + "\"}");
                } else {
                	
                	// Not Enough Balance
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": false, \"message\": \"Insufficient balance.\"}");
                }
            } else {
            	//No matching user found
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"User not found.\"}");
            }
        } catch (Exception e) {
        	
        	// Rollback on error and send failure response
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Error processing transaction: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
        	
        	// Clean up JDBC resources
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    
    /**
     * Fetches the current stock price for the given ticker symbol using Finnhub API.
     * @param ticker The stock symbol (e.g., "AAPL")
     * @return The current price, or -1 if unavailable
     */
    private double getStockPrice(String ticker) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            String apiKey = "csrv5m9r01qj3u0ouflgcsrv5m9r01qj3u0oufm0";
            URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            // Read and buffer the API response
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            
            // Parse the "c" (current price) field from the response JSON
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