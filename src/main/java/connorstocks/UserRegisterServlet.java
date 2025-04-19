package connorstocks;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONObject;

/**
 * Servlet that handles user registration.
 * Accepts username, password, and email, checks for duplicates, and inserts the user into the database.
 */
@WebServlet("/register")
public class UserRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Load the MySQL JDBC driver once when the class is loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles POST requests to register a new user.
     * If username or email already exists, returns an error message.
     * Otherwise, inserts a new user with a starting balance and 0 stock holdings.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract registration fields from request
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Default initial values for a new user
        int balance = 50000; 
        int tsla = 0, aapl = 0, amd = 0, nvda = 0;

        // Database connection settings
        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {

            // Check if username or email already exists
            if (userExists(connection, username, email)) {
                response.getWriter().write("Username or Email already exists");
                return;
            }

            // Insert new user into the database
            String sql = "INSERT INTO userInfo (username, userPassword, userEmail, balance, TSLA, AAPL, AMD, NVDA) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, email);
                statement.setInt(4, balance);
                statement.setInt(5, tsla);
                statement.setInt(6, aapl);
                statement.setInt(7, amd);
                statement.setInt(8, nvda);

                int result = statement.executeUpdate();

                if (result > 0) {
                    response.getWriter().write("Success");
                } else {
                    response.getWriter().write("Error 1"); // Could not insert user
                }
            }

        } catch (SQLException e) {
            response.getWriter().write("Error 2"); // Database error
            e.printStackTrace();
        }
    }

    /**
     * Helper method to check if a username or email already exists in the database.
     * Prevents duplicate registrations.
     * Takes argumetnts: connection (an active DB connection), username, email
     * Return true if a duplicate exists, false otherwise
     */
    private boolean userExists(Connection connection, String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM userInfo WHERE username = ? OR userEmail = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
