package cmcgartl_CSCI201_Assignment4;

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

@WebServlet("/stockInfo")
public class StockInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final String apiKey = "csrv5m9r01qj3u0ouflgcsrv5m9r01qj3u0oufm0";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ticker = request.getParameter("ticker");
        HttpClient client = HttpClient.newHttpClient();

        String profileResponse = null;
        String quoteResponse = null;
        try {
            profileResponse = fetchStockData(client, "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token=" + apiKey);
            quoteResponse = fetchStockData(client, "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to fetch stock data.\"}");
            return;
        }

        
        String combinedResponse = "{\"profile\":" + profileResponse + ", \"quote\":" + quoteResponse + "}";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(combinedResponse);
        out.flush();
    }

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