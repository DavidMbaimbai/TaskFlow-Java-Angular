package com.discovery.repository.implementation;
import com.discovery.entity.User;
import com.discovery.exception.ApiException;
import com.discovery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.discovery.query.UserQuery.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final JdbcClient jdbc;
    @Override
    public User getUserByUsername(String username) {
        try {
            return jdbc.sql(SELECT_USER_BY_USERNAME_QUERY).param("username", username).query(User.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found by username %s ", username ));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again ");
        }
    }
}
