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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

/**
 * Servlet that processes stock trade requests for the authenticated user.
 * Handles both "buy" and "sell" actions based on current market prices.
 * Responds with a JSON message indicating trade success or failure.
 */
@WebServlet("/tradeStock")
public class PortfolioTradeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Handles POST requests to buy or sell stocks.
     * Takes 'ticker', 'quantity', and 'action' (buy or sell) as parameters.
     * Responds with a JSON object indicating the result.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";
        		
    	// Validate session and user authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }

        // Get trade request parameters
        String username = (String) session.getAttribute("user");
        String ticker = request.getParameter("ticker");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String action = request.getParameter("action"); // buy or sell

        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();

        try {
        	conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            conn.setAutoCommit(false); // begin transaction

            // Execute trade and commit or rollback based on result
            if (!executeTrade(conn, username, ticker, quantity, action)) {
                conn.rollback();
                jsonResponse.put("message", "Failed to execute trade");
            } else {
                conn.commit();
                jsonResponse.put("message", "Trade successful");
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // Rollback transaction on exception
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            jsonResponse.put("message", "Error processing transaction");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
        	
            // Clean up connection and return JSON response
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        }
    }

    /**
     * Executes a buy or sell stock trade in the database.
     * Validates user balance and holdings before applying the trade.
     * return true if trade was successful, false otherwise
     */
    private boolean executeTrade(Connection conn, String username, String ticker, int quantity, String action)
            throws SQLException, IOException {

        // Query user's balance and current stock quantity
        String userQuery = "SELECT balance, " + ticker + " FROM userInfo WHERE username = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setString(1, username);
        ResultSet userData = userStmt.executeQuery();

        if (!userData.next()) {
            return false;
        }

        double currentPrice = getStockPrice(ticker);
        double balance = userData.getDouble("balance");
        int stockQuantity = userData.getInt(ticker);

        if (action.equals("buy")) {
            double cost = quantity * currentPrice;
            if (balance < cost) {
                return false;
            }
            balance -= cost;
            stockQuantity += quantity;

        } else if (action.equals("sell")) {
            System.out.println("selling");
            if (stockQuantity < quantity) {
                return false;
            }
            double earnings = quantity * currentPrice;
            balance += earnings;
            stockQuantity -= quantity;

        } else {
            return false;
        }

        // Update user's balance and holdings in the database
        String updateQuery = "UPDATE userInfo SET balance = ?, " + ticker + " = ? WHERE username = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
        updateStmt.setDouble(1, balance);
        updateStmt.setInt(2, stockQuantity);
        updateStmt.setString(3, username);

        return updateStmt.executeUpdate() > 0;
    }

    /**
     * Retrieves the current price of a stock from the Finnhub API.
     * Parses the c (current price) field from the API JSON response.
     */
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

            // Parse "c": current price
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
