package connorstocks;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Servlet that checks whether a user is currently logged in.
 * Intended to be called by the frontend (e.g., via AJAX) to verify session status.
 * Responds with either "loggedIn" or "notLoggedIn" as plain text.
 */
@WebServlet("/checkLogin")
public class CheckLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Handles GET requests to verify login status.
     * Checks for an existing session and presence of the "user" attribute.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// Retrieve existing session
        HttpSession session = request.getSession(false); // Do not create a new session if one does not exist
        
        // If session exists and user is logged in, return "loggedIn"
        if (session != null && session.getAttribute("user") != null) {
            response.getWriter().write("loggedIn");
            
         // Otherwise, return "notLoggedIn"
        } else {
            response.getWriter().write("notLoggedIn");
        }
    }
}