package com.authorization.server.query;

public class UserQuery {
    public static final String SELECT_USER_BY_USER_UUID_QUERY =
            """
            SELECT * FROM users
            """;
    public static final String SELECT_USER_BY_EMAIL_QUERY =
            """
            SELECT * FROM email
            """;
    public static final String RESET_LOGIN_ATTEMPTS_QUERY =
            """
            SELECT * FROM email
            """;
    public static final String UPDATE_LOGIN_ATTEMPTS_QUERY =
            """
            SELECT * FROM email
            """;
    public static final String SET_LAST_LOGIN_QUERY =
            """
            SELECT * FROM email
            """;
    public static final String INSERT_NEW_DEVICE_QUERY =
            """
            SELECT * FROM email
            """;
}
