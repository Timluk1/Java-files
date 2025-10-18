package org.example.dao;

import org.example.util.DatabaseConfig;

import java.sql.*;

public class UserDaoJdbcImpl implements UserDao {

    public UserDaoJdbcImpl() {
    }

    @Override
    public User findByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password_hash, email, image_url, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEmail(rs.getString("email"));
                    u.setImageUrl(rs.getString("image_url"));
                    u.setCreatedAt(rs.getTimestamp("created_at"));
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public User findById(Long id) throws Exception {
        String sql = "SELECT id, username, password_hash, email, image_url, created_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEmail(rs.getString("email"));
                    u.setImageUrl(rs.getString("image_url"));
                    u.setCreatedAt(rs.getTimestamp("created_at"));
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public Long create(User user) throws Exception {
        String sql = "INSERT INTO users (username, password_hash, email, image_url, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getImageUrl());
            Timestamp created = user.getCreatedAt() != null ? user.getCreatedAt() : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(5, created);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    user.setId(id);
                    user.setCreatedAt(created);
                    return id;
                } else {
                    // As a fallback for PostgreSQL, try SELECT id by unique username
                    try (PreparedStatement ps2 = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                        ps2.setString(1, user.getUsername());
                        try (ResultSet rs = ps2.executeQuery()) {
                            if (rs.next()) {
                                long id = rs.getLong(1);
                                user.setId(id);
                                return id;
                            }
                        }
                    }
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
}
