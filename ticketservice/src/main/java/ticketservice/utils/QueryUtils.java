package ticketservice.utils;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replace;
import static ticketservice.query.TicketQuery.*;
public class QueryUtils {

    public static String createSelectTicketsQuery(String status, String type, String filter) {
        var query = getStringBuilder(SELECT_TICKETS_QUERY);
        if(isNotBlank(status)) {
            query.append(" AND s.status = :status");
        }
        if(isNotBlank(type)) {
            query.append(" AND typ.type = :type");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* :filter");
        }
        query.append(" GROUP BY t.ticket_id, s.status, typ.type_id, pr.priority_id ORDER BY t.created_at DESC LIMIT :size OFFSET :offset");
        return replace(query.toString(), "\\n", "");
    }

    public static String createSelectUserTicketsQuery(String status, String type, String filter) {
        var query = getStringBuilder(SELECT_TICKETS_BY_USER_UUID_QUERY);
        if(isNotBlank(status)) {
            query.append(" AND s.status = :status");
        }
        if(isNotBlank(type)) {
            query.append(" AND typ.type = :type");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* :filter");
        }
        query.append(" GROUP BY t.ticket_id, s.status, typ.type_id, pr.priority_id ORDER BY t.created_at DESC LIMIT :size OFFSET :offset");
        return replace(query.toString(), "\\n", "");
    }

    public static String createSelectPagesQuery(String status, String type, String filter) {
        var query = getStringBuilder(SELECT_PAGE_NUMBER_QUERY);
        if(isNotBlank(status)) {
            query.append(" AND s.status = :status");
        }
        if(isNotBlank(type)) {
            query.append(" AND typ.type = :type");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* :filter");
        }
        return replace(query.toString(), "\\n", "");
    }

    public static String createSelectUserPagesQuery(String status, String type, String filter) {
        var query = getStringBuilder(SELECT_USER_PAGE_NUMBER_QUERY);
        if(isNotBlank(status)) {
            query.append(" AND s.status = :status");
        }
        if(isNotBlank(type)) {
            query.append(" AND typ.type = :type");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* :filter");
        }
        return replace(query.toString(), "\\n", "");
    }

    public static String createTicketReportQuery(String filter, String fromDate, String toDate, List<String> statuses, List<String> types, List<String> priorities) {
        var query = getStringBuilder("SELECT t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE 1 = 1");
        if(isNotBlank(fromDate)) {
            query.append(" AND t.created_at >= " + "'" + fromDate + "'");
        }if(isNotBlank(toDate)) {
            query.append(" AND t.created_at <= " + "'" + toDate + "'");
        }if(!statuses.isEmpty()) {
            var inStatuses = "";
            for (var status: statuses) {
                inStatuses = inStatuses + "'" + status + "',";
            }
            inStatuses = inStatuses.substring(0, inStatuses.length() - 1);
            query.append(" AND s.status IN (" + inStatuses + ")");
        }
        if(!types.isEmpty()) {
            var inTypes = "";
            for (var type: types) {
                inTypes = inTypes + "'" + type + "',";
            }
            inTypes = inTypes.substring(0, inTypes.length() - 1);
            query.append(" AND typ.type IN (" + inTypes + ")");
        }
        if(!priorities.isEmpty()) {
            var Inpriorities = "";
            for (var priority: priorities) {
                Inpriorities = Inpriorities + "'" + priority + "',";
            }
            Inpriorities = Inpriorities.substring(0, Inpriorities.length() - 1);
            query.append(" AND pr.priority IN (" + Inpriorities + ")");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* " + "'" + filter + "'");
        }
        query.append(" ORDER BY t.created_at DESC");
        return replace(query.toString(),"\\n", "");
    }

    public static String createTicketReportQuery(String userUuid, String filter, String fromDate, String toDate, List<String> statuses, List<String> types, List<String> priorities) {
        var query = getStringBuilder("SELECT t.ticket_id, t.ticket_uuid, t.title, t.description, t.progress, t.due_date, t.created_at, t.updated_at, s.status, typ.type, pr.priority FROM tickets t JOIN ticket_statuses ts ON t.ticket_id = ts.ticket_id JOIN ticket_types tt ON t.ticket_id = tt.ticket_id JOIN ticket_priorities tp ON t.ticket_id = tp.ticket_id JOIN statuses s ON s.status_id = ts.status_id JOIN types typ ON typ.type_id = tt.type_id JOIN priorities pr ON pr.priority_id = tp.priority_id WHERE 1 = 1");
        if(isNotBlank(fromDate)) {
            query.append(" AND t.created_at >= " + "'" + fromDate + "'");
        }if(isNotBlank(toDate)) {
            query.append(" AND t.created_at <= " + "'" + toDate + "'");
        }if(!statuses.isEmpty()) {
            var inStatuses = "";
            for (var status: statuses) {
                inStatuses = inStatuses + "'" + status + "',";
            }
            inStatuses = inStatuses.substring(0, inStatuses.length() - 1);
            query.append(" AND s.status IN (" + inStatuses + ")");
        }
        if(!types.isEmpty()) {
            var inTypes = "";
            for (var type: types) {
                inTypes = inTypes + "'" + type + "',";
            }
            inTypes = inTypes.substring(0, inTypes.length() - 1);
            query.append(" AND typ.type IN (" + inTypes + ")");
        }
        if(!priorities.isEmpty()) {
            var Inpriorities = "";
            for (var priority: priorities) {
                Inpriorities = Inpriorities + "'" + priority + "',";
            }
            Inpriorities = Inpriorities.substring(0, Inpriorities.length() - 1);
            query.append(" AND pr.priority IN (" + Inpriorities + ")");
        }
        if(isNotBlank(filter)) {
            query.append(" AND t.title ~* " + "'" + filter + "'");
        }
        query.append(" AND u.user_uuid = " + "'" + userUuid + "'");
        query.append(" ORDER BY t.created_at DESC");
        return replace(query.toString(),"\\n", "");
    }

    private static StringBuilder getStringBuilder(String query) {
        return new StringBuilder(query);
    }




















}