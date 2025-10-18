package org.example.servlets;

import org.example.dao.User;
import org.example.services.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/auth")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User u = authService.authenticate(username, password);
            if (u == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("invalid_credentials");
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", u.getId());
            session.setAttribute("username", u.getUsername());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("ok");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("error: " + e.getMessage());
        }
    }
}
