package cmcgartl_CSCI201_Assignment4;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/login")
public class loginServlet extends HttpServlet {
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
		System.out.println("logged in");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
 

        String jdbcURL = "jdbc:mysql://localhost:3306/ConnorsDatabase2?user=root&password=Wg600951CM";
        String dbUser = "root";
        String dbPassword = "Wg600951CM";

        String sql = "SELECT * FROM userInfo WHERE username = ? AND userPassword = ?";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                response.getWriter().write("Success");
            } else {
                response.getWriter().write("Invalid login credentials");
            }
        } catch (SQLException e) {
            response.getWriter().write("Database error");
            e.printStackTrace();
        }
    }
}