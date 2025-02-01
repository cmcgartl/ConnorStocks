package cmcgartl_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONObject;

@WebServlet("/portfolio")
public class PortfolioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	System.out.println("got into portfolio servlet!!");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }

        String username = (String) session.getAttribute("user");
        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2?user=root&password=Wg600951CM";

        try (Connection connection = DriverManager.getConnection(jdbcURL)) {
            String sql = "SELECT balance, TSLA, AAPL, AMD, NVDA, COIN, ROKU, RBLX FROM userInfo WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    double balance = resultSet.getDouble("balance");
                    JSONObject portfolioData = new JSONObject();
                    portfolioData.put("balance", balance);

                    JSONObject stocks = new JSONObject();
                    stocks.put("TSLA", getStockDetails("TSLA", resultSet.getInt("TSLA"), username));
                    stocks.put("AAPL", getStockDetails("AAPL", resultSet.getInt("AAPL"), username));
                    stocks.put("AMD", getStockDetails("AMD", resultSet.getInt("AMD"), username));
                    stocks.put("NVDA", getStockDetails("NVDA", resultSet.getInt("NVDA"), username));
                    stocks.put("COIN", getStockDetails("COIN", resultSet.getInt("COIN"), username));
                    stocks.put("RBLX", getStockDetails("RBLX", resultSet.getInt("RBLX"), username));
                    stocks.put("ROKU", getStockDetails("ROKU", resultSet.getInt("ROKU"), username));

                    // To calculate the total account value, sum up the market values and the balance
                    double totalAccountValue = balance + calculateTotalMarketValue(stocks);
                    portfolioData.put("totalAccountValue", totalAccountValue);
                    portfolioData.put("stocks", stocks);

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(portfolioData.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("User data not found");
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error");
            e.printStackTrace();
        }
    }
    //lines 66 - 86 from chatgpt: "How can I convert the stock details into a Json object" accessed: 4/22/24
    private JSONObject getStockDetails(String ticker, int quantityOwned, String user) throws IOException {
        // Assume you have a method to get the current and previous prices, and the average cost
    	double currentPrice = getStockPrice(ticker);
        double change = getPriceChange(ticker);
        

        double totalCost = currentPrice * quantityOwned;
        double marketValue = currentPrice * quantityOwned;
        double avgCost = 0;
        if(quantityOwned > 0) {
        avgCost = totalCost / quantityOwned;
        }
        JSONObject stockDetails = new JSONObject();
        stockDetails.put("quantity", quantityOwned);
        stockDetails.put("avgCost", avgCost);
        stockDetails.put("totalCost", totalCost);
        stockDetails.put("change", change);
        stockDetails.put("currentPrice", currentPrice);
        stockDetails.put("marketValue", marketValue);

        return stockDetails;
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


    private double getPriceChange(String ticker) throws IOException {
    	HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            String apiKey = "cnv61k1r01qub9j0css0cnv61k1r01qub9j0cssg";
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
            String keyToFind = "\"d\":";
            int startIndex = responseStr.indexOf(keyToFind) + keyToFind.length();
            int endIndex = responseStr.indexOf(",", startIndex);
            String priceStr = responseStr.substring(startIndex, endIndex).trim();
            return Double.parseDouble(priceStr);
        } finally {
            if (in != null) in.close();
            if (connection != null) connection.disconnect();
        }
    }

    private double calculateTotalMarketValue(JSONObject stocks) {
        double totalMarketValue = 0;
        for (String key : stocks.keySet()) {
            JSONObject stock = stocks.getJSONObject(key);
            totalMarketValue += stock.getDouble("marketValue");
        }
        return totalMarketValue;
    }
}
