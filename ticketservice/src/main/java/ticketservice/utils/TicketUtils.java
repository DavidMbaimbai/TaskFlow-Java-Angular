package ticketservice.utils;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;
import java.util.function.Supplier;
public class TicketUtils {

    public static Supplier<String> randomUUID = () -> UUID.randomUUID().toString();

    public static String getFileUri(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/ticket/files/" + filename).toUriString();
    }
}