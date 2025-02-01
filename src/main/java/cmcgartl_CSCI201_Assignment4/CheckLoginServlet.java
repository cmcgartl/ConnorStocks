package cmcgartl_CSCI201_Assignment4;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/checkLogin")
public class CheckLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Do not create a new session if one does not exist
        if (session != null && session.getAttribute("user") != null) {
        
            response.getWriter().write("loggedIn");
        } else {
        
            response.getWriter().write("notLoggedIn");
        }
    }
}