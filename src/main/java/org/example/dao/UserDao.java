package org.example.dao;

public interface UserDao {
    User findByUsername(String username) throws Exception;
    User findById(Long id) throws Exception;
    Long create(User user) throws Exception;
}

