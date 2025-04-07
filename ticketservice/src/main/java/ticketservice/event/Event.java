package ticketservice.event;
import lombok.*;
import ticketservice.enumeration.EventType;

import java.util.Map;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private EventType eventType;
    private Map<String, ?> data;
}