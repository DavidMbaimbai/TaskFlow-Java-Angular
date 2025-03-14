package com.discovery.repository;
import com.discovery.entity.User;
public interface UserRepository {
    User getUserByUsername(String username);
}
