package connorstocks;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONObject;

/**
 * Servlet that fetches and returns real-time stock data for a given ticker symbol.
 * Combines a company profile and current quote into a single JSON response.
 */
@WebServlet("/stockInfo")
public class StockInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Finnhub API key (used for both profile and quote requests)
    private final String apiKey = "csrv5m9r01qj3u0ouflgcsrv5m9r01qj3u0oufm0";

    /**
     * Handles GET requests for stock data.
     * Expects a "ticker" parameter, fetches both profile and quote info, and returns combined JSON.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract the ticker symbol from request parameters
        String ticker = request.getParameter("ticker");
        HttpClient client = HttpClient.newHttpClient();

        String profileResponse;
        String quoteResponse;

        try {
        	
            // Fetch stock profile and current quote from Finnhub API
            profileResponse = fetchStockData(client, "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token=" + apiKey);
            quoteResponse = fetchStockData(client, "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey);
        } catch (IOException | InterruptedException e) {
        	
            // On error, send a JSON error response with 500 status
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to fetch stock data.\"}");
            return;
        }

        // Combine both JSON responses under 'profile' and 'quote' fields
        String combinedResponse = "{\"profile\":" + profileResponse + ", \"quote\":" + quoteResponse + "}";

        // Set content type and return combined JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(combinedResponse);
        out.flush();
    }

    /**
     * Makes an HTTP GET request to the given URL and returns the JSON response body.
     * Throws IOException on non-200 responses or network failures.
     */
    private String fetchStockData(HttpClient client, String urlString) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .header("Accept", "application/json")
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Error while fetching stock data, HTTP status code: " + response.statusCode());
        }
    }
}