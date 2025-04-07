package ticketservice.model;
import lombok.*;
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    private Long ticketId;
    private String ticketUuid;
    private String title;
    private String description;
    private String status;
    private String type;
    private String priority;
    private int fileCount;
    private int commentCount;
    private int progress;
    private String dueDate;
    private String createdAt;
    private String updatedAt;
}