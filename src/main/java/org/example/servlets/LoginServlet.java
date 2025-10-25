package org.example.servlets;

import org.example.dao.User;
import org.example.services.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet("/auth")
@MultipartConfig
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String username = readPartAsString(req.getPart("username"), req.getCharacterEncoding());
        String password = readPartAsString(req.getPart("password"), req.getCharacterEncoding());

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

    private String readPartAsString(Part part, String charset) throws IOException {
        if (part == null) return null;
        Charset cs = charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;
        try (InputStream is = part.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, cs))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
