package connorstocks;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Servlet that handles user login requests.
 * Authenticates credentials against the database and establishes a session on success.
 */
@WebServlet("/login")
public class loginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

 // Load MySQL JDBC driver when the class is first loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handles POST requests for user login.
     * Verifies the provided username and password against the userInfo table.
     * On success, creates a session and stores the username; otherwise, returns an error message.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
		// Retrieve credentials from request parameters
        String username = request.getParameter("username");
        String password = request.getParameter("password");
 
        // Database connection details
        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDataBase2";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";

        // SQL query to validate credentials
        String sql = "SELECT * FROM userInfo WHERE username = ? AND userPassword = ?";

        try (
            // Open database connection and prepare statement
        	Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            PreparedStatement statement = connection.prepareStatement(sql)
            ) {
            statement.setString(1, username);
            statement.setString(2, password);

            //execute the query
            ResultSet result = statement.executeQuery();
            
            if (result.next()) {
            	
            	// Credentials matched — create session and return success
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                response.getWriter().write("Success");
            } else {
            	
            	  // No match found
                response.getWriter().write("Invalid login credentials");
            }
            
        } catch (SQLException e) {
        	
            // Handle DB connection or query error
            response.getWriter().write("Database error");
            e.printStackTrace();
        }
    }
}