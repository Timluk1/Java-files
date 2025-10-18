package org.example.servlets;

import org.example.dao.User;
import org.example.services.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

@WebServlet("/register")
@MultipartConfig
public class RegistrationServlet extends HttpServlet {
    private static final String FILE_PREFIX = System.getProperty("java.io.tmpdir");

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String username = null;
        String password = null;
        String email = null;
        String imageUrl = null;

        String contentType = req.getContentType();
        Part part = null;

        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            // request is multipart/form-data (Postman "form-data"). Read text parts explicitly.
            username = readPartAsString(req.getPart("username"), req.getCharacterEncoding());
            password = readPartAsString(req.getPart("password"), req.getCharacterEncoding());
            email = readPartAsString(req.getPart("email"), req.getCharacterEncoding());
            part = req.getPart("file");
        } else {
            // normal form submit (application/x-www-form-urlencoded)
            username = req.getParameter("username");
            password = req.getParameter("password");
            email = req.getParameter("email");
            // no file support for urlencoded
        }

        if (part != null && part.getSize() > 0) {
            String submitted = part.getSubmittedFileName();
            String fileName = submitted == null ? "upload.bin" : Paths.get(submitted).getFileName().toString();

            Path target = Paths.get(FILE_PREFIX).resolve(fileName);

            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (InputStream content = part.getInputStream()) {
                Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
                imageUrl = target.toAbsolutePath().toString();
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("error saving file: " + e.getMessage());
                return;
            }
        }

        try {
            User u = authService.register(username, password, email, imageUrl);

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", u.getId());
            session.setAttribute("username", u.getUsername());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("registered");
        } catch (IllegalStateException ise) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("username_taken");
        } catch (IllegalArgumentException iae) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(iae.getMessage());
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
