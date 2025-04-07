package ticketservice.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private Long taskId;
    private String taskUuid;
    private String name;
    private String description;
    private String status;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String dueDate;
    private String createdAt;
    private String updatedAt;
}