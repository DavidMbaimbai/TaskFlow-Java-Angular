package ticketservice.query;
public class TicketQuery {
    public static final String SELECT_TICKETS_QUERY =
                               """
                               SELECT COUNT(DISTINCT(c.comment_id)) AS comment_count, COUNT(DISTINCT(f.file_id)) AS file_count, t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id LEFT JOIN files f ON t.ticket_id = f.ticket_id LEFT JOIN comments c ON t.ticket_id = c.ticket_id WHERE 1 = 1
                               """;
    public static final String SELECT_TICKETS_BY_USER_UUID_QUERY =
                               """
                               SELECT COUNT(DISTINCT(c.comment_id)) AS comment_count, COUNT(DISTINCT(f.file_id)) AS file_count, t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id LEFT JOIN files f ON t.ticket_id = f.ticket_id LEFT JOIN comments c ON t.ticket_id = c.ticket_id WHERE u.user_uuid = :userUuid
                               """;
    public static final String SELECT_PAGE_NUMBER_QUERY =
                               """
                               SELECT (CEILING(COUNT(*) / :size ::NUMERIC(10, 5))) AS pages FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE 1 = 1
                               """;
    public static final String SELECT_USER_PAGE_NUMBER_QUERY =
                               """
                               SELECT (CEILING(COUNT(*) / :size ::NUMERIC(10, 5))) AS pages FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE u.user_uuid = :userUuid
                               """;
    public static final String CREATE_TICKET_FUNCTION =
                               """
                               SELECT * FROM create_ticket(:ticketUuid, :userUuid, :title, :description, :type, :priority)
                               """;
    public static final String SELECT_TICKET_BY_USER_UUID_QUERY =
                               """
                               SELECT t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE u.user_uuid = :userUuid AND t.ticket_uuid = :ticketUuid
                               """;
    public static final String SELECT_TICKET_QUERY =
                               """
                               SELECT t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN users u ON t.user_id = u.user_id JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE t.ticket_uuid = :ticketUuid
                               """;
    public static final String SELECT_COMMENTS_QUERY =
                               """
                               SELECT c.comment_id, c.comment_uuid, c.comment, c.edited, c.created_at, c.updated_at, u.user_uuid, u.first_name, u.last_name, u.image_url FROM comments c JOIN tickets t ON t.ticket_id = c.ticket_id JOIN users u ON u.user_id = c.user_id WHERE t.ticket_uuid = :ticketUuid ORDER BY c.created_at DESC
                               """;
    public static final String SELECT_TICKET_TASKS_QUERY =
                               """
                               SELECT u.first_name, u.last_name, u.image_url, t.task_id, t.task_uuid, t.name, t.description, s.status, t.due_date, t.created_at, t.updated_at FROM tasks t JOIN users u ON t.assignee_id = u.user_id JOIN task_statuses ts ON t.task_id = ts.task_id JOIN statuses s ON ts.status_id = s.status_id WHERE ticket_id = (SELECT ticket_id FROM tickets WHERE ticket_uuid = :ticketUuid) ORDER BY created_at DESC
                               """;
    public static final String CREATE_COMMENT_FUNCTION =
                               """
                               SELECT * FROM create_comment(:commentUuid, :userUuid, :ticketUuid, :comment)
                               """;
    public static final String SELECT_FILES_QUERY =
                               """
                               SELECT * FROM files WHERE ticket_id = (SELECT ticket_id FROM tickets WHERE ticket_uuid = :ticketUuid) ORDER BY created_at DESC
                               """;
    public static final String DELETE_FILES_QUERY =
                               """
                               DELETE FROM files WHERE file_uuid = :fileUuid
                               """;
    public static final String UPDATE_COMMENT_FUNCTION =
                               """
                               SELECT * FROM update_comment(:commentUuid, :comment)
                               """;
    public static final String DELETE_COMMENT_QUERY =
                               """
                               DELETE FROM comments WHERE comment_uuid = :commentUuid
                               """;
    public static final String UPDATE_TICKET_FUNCTION =
                               """
                               SELECT * FROM update_ticket(:ticketUuid, :title, :description, :progress, :dueDate, :status, :type, :priority)
                               """;
    public static final String CREATE_TASK_FUNCTION =
                               """
                               SELECT * FROM create_task(:userUuid, :ticketUuid, :taskUuid, :name, :description, :status)
                               """;
    public static final String SAVE_TICKET_FILE_FUNCTION =
                               """
                               SELECT * FROM save_ticket_file(:fileUuid, :ticketId, :filename, :size, :formattedSize, :extension, :uri)
                               """;
    public static final String SELECT_FILES_BY_USER_UUID_QUERY =
                               """
                               SELECT * FROM files WHERE file_uuid = :fileUuid
                               """;
    public static final String TOGGLE_ACCOUNT_LOCKED_FUNCTION =
                               """
                               SELECT * FROM toggle_account_locked(:userUuid)
                               """;
    public static final String TOGGLE_ACCOUNT_ENABLED_FUNCTION =
                               """
                               SELECT * FROM toggle_account_enabled(:userUuid)
                               """;
    public static final String UPDATE_USER_PASSWORD_QUERY =
                               """
                               UPDATE credentials SET password = :password WHERE user_id = (SELECT user_id FROM users WHERE user_uuid = :userUuid)
                               """;
    public static final String SELECT_USER_PASSWORD_QUERY =
                               """
                               SELECT c.password FROM credentials c JOIN users u ON c.user_id = u.user_id WHERE u.user_uuid = :userUuid
                               """;
    public static final String UPDATE_USER_ROLE_FUNCTION =
                               """
                               SELECT * FROM update_user_role(:userUuid, :role)
                               """;
    public static final String SELECT_USERS_QUERY =
                               """
                               SELECT r.name AS role, r.authority AS authorities, u.qr_code_image_uri, u.member_id, u.account_non_expired, u.account_non_locked, u.created_at, u.email, u.username, u.enabled, u.first_name, u.user_id, u.image_url, u.last_login, u.last_name, u.mfa, u.updated_at, u.user_uuid, u.bio, u.phone, u.address FROM users u JOIN user_roles ur ON ur.user_id = u.user_id JOIN roles r ON r.role_id = ur.role_id LIMIT 100
                               """;
    public static final String SELECT_TICKET_ASSIGNEE_QUERY =
                               """
                               SELECT u.user_id, u.user_uuid, u.first_name, u.last_name, u.email, u.image_url FROM users u JOIN tickets t ON u.user_id = t.assignee_id WHERE t.ticket_uuid = :ticketUuid
                               """;
    public static final String SELECT_USER_CREDENTIAL_QUERY =
                               """
                               SELECT c.credential_id, c.credential_uuid, c.password, c.created_at, c.updated_at FROM credentials c JOIN users u ON c.user_id = u.user_id WHERE u.user_uuid = :userUuid
                               """;
    public static final String SELECT_DEVICES_QUERY =
                               """
                               SELECT * FROM devices WHERE user_id = (SELECT user_id FROM users WHERE user_uuid = :userUuid) ORDER BY created_at DESC LIMIT 15
                               """;
    public static final String CREATE_PASSWORD_TOKEN_QUERY =
                               """
                               INSERT INTO password_tokens (user_id, token) VALUES (:userId, :token)
                               """;
}