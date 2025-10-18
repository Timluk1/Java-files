package org.example.servlets;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;

@WebServlet("/upload")
@MultipartConfig
public class MainServlet extends HttpServlet {
    // Используем системный временный каталог вместо жесткого "/tmp" — это работает и на Windows
    private static final String FILE_PREFIX = System.getProperty("java.io.tmpdir");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("text/plain; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Отсутствует файл в запросе или файл пустой.");
            return;
        }

        // Безопасно извлекаем только имя файла (убираем возможные пути)
        String submitted = part.getSubmittedFileName();
        String fileName = submitted == null ? "upload.bin" : Paths.get(submitted).getFileName().toString();

        Path target = Paths.get(FILE_PREFIX).resolve(fileName);

        // Убедимся, что папка для файла существует
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (InputStream content = part.getInputStream()) {
            // Копируем содержимое, перезаписывая при необходимости
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Файл успешно сохранён: " + target.toAbsolutePath());

        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Ошибка при сохранении файла: " + e.getMessage());
        }
    }
}
