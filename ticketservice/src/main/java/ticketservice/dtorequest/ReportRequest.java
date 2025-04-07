package ticketservice.dtorequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportRequest {
    private String filter;
    private String fromDate;
    private String toDate;
    private List<String> statuses;
    private List<String> types;
    private List<String> priorities;
}