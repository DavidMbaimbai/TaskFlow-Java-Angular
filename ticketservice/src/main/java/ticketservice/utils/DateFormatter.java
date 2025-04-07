package ticketservice.utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
public class DateFormatter {
    public static final String DATE_FORMATTER = "yyyy-MM-dd hh:mm:ss";

    public static String shortDate(String date) {
        return date.split(" ")[0];
    }

    public static String today(String formatter) {
        var cal = Calendar.getInstance();
        cal.setTime(new Date());
        var dateFormat = new SimpleDateFormat(formatter);
        return dateFormat.format(cal.getTime());
    }

    public static String formattedDate() {
        var cal = Calendar.getInstance();
        cal.setTime(new Date());
        var dayNumberSuffix = getDayNumberSuffix(cal.get(Calendar.DAY_OF_MONTH));
        var dateFomat = new SimpleDateFormat("MMMM d'" + dayNumberSuffix + ", ' yyyy - hh:mm:ss a");
        return dateFomat.format(cal.getTime());
    }

    private static String getDayNumberSuffix(int day) {
        if(day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}