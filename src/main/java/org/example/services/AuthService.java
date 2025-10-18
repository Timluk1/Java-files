package org.example.services;

import org.example.dao.User;
import org.example.dao.UserDao;
import org.example.dao.UserDaoJdbcImpl;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDaoJdbcImpl();
    }

    public User register(String username, String password, String email, String imageUrl) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }

        User existing = userDao.findByUsername(username);
        if (existing != null) {
            throw new IllegalStateException("username_taken");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(hash);
        u.setEmail(email);
        u.setImageUrl(imageUrl);

        Long id = userDao.create(u);
        u.setId(id);

        return u;
    }

    public User authenticate(String username, String password) throws Exception {
        if (username == null || password == null) return null;
        User u = userDao.findByUsername(username);
        if (u == null) return null;
        if (u.getPasswordHash() == null) return null;

        boolean ok = BCrypt.checkpw(password, u.getPasswordHash());
        return ok ? u : null;
    }
}
