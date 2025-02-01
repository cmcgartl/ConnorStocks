package cmcgartl_CSCI201_Assignment4;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONObject;

@WebServlet("/register")
public class UserRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        int balance = 50000; 
        int tsla = 0, aapl = 0, amd = 0, nvda = 0; 

        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2?user=root&password=Wg600951CM";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            
            if (userExists(connection, username, email)) {
                response.getWriter().write("Username or Email already exists");
                return;
            }

            String sql = "INSERT INTO userInfo (username, userPassword, userEmail, balance, TSLA, AAPL, AMD, NVDA) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
                    response.getWriter().write("Error 1");
                }
            }
        } catch (SQLException e) {
            response.getWriter().write("Error 2");
            e.printStackTrace();
        }
    }

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
