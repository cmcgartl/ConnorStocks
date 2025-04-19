package connorstocks;

import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Servlet that logs out the current user by invalidating their session.
 * Responds with a plain text confirmation message.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Handles GET requests to log the user out.
     * Invalidates the session if one exists and returns a confirmation message.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve the current session without creating a new one
        HttpSession session = request.getSession(false);

        // If session exists, invalidate it to log the user out
        if (session != null) {
            session.invalidate();
        }

        // Respond to the client with confirmation
        response.getWriter().write("Logged out");
    }
}