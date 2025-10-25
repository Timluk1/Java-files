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
import org.example.util.CloudinaryUtil;
import java.util.stream.Collectors;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/register")
@MultipartConfig
public class RegistrationServlet extends HttpServlet {
    public static final String FILE_PREFIX = "/tmp";
    public static final int DIRECTORIES_COUNT = 100;
    private final AuthService authService = new AuthService();
    private final Cloudinary cloudinaryUtil = CloudinaryUtil.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        String username = null;
        String password = null;
        String email = null;
        String imageUrl = "";

        String contentType = req.getContentType();
        Part part = null;

        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            // multipart/form-data (e.g. Postman form-data)
            username = readPartAsString(req.getPart("username"), req.getCharacterEncoding());
            password = readPartAsString(req.getPart("password"), req.getCharacterEncoding());
            email = readPartAsString(req.getPart("email"), req.getCharacterEncoding());
            part = req.getPart("file");
        } else {
            // application/x-www-form-urlencoded
            username = req.getParameter("username");
            password = req.getParameter("password");
            email = req.getParameter("email");
        }

        if (part != null && part.getSize() > 0) {
            String filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();

            File file = new File(FILE_PREFIX + File.separator
                    + Math.abs(filename.hashCode() % DIRECTORIES_COUNT) + File.separator + filename);

            InputStream content = part.getInputStream();
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[content.available()];
            content.read(buffer);
            outputStream.write(buffer);
            outputStream.close();

            Map<?, ?> fileUpload = cloudinaryUtil.uploader().upload(file, new HashMap());
            imageUrl = (String) fileUpload.get("secure_url");
        }

        if (username == null || username.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("missing_username");
            return;
        }
        if (password == null || password.length() < 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("password_too_short");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("missing_email");
            return;
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
