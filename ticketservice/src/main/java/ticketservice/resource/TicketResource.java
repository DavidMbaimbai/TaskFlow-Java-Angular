package ticketservice.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ticketservice.domain.Response;
import ticketservice.dtorequest.ReportRequest;
import ticketservice.dtorequest.TicketRequest;
import ticketservice.service.TicketService;
import ticketservice.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static ticketservice.constant.Constants.FILE_NAME_HEADER;
import static ticketservice.utils.RequestUtils.getResponse;


@RestController
@AllArgsConstructor
@RequestMapping("/ticket")
public class TicketResource {
    private final TicketService ticketService;
    private final UserService userService;

    @GetMapping("/list")
    public ResponseEntity<Response> getTickets(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "12") int size, @RequestParam(value = "status", defaultValue = "") String status, @RequestParam(value = "type", defaultValue = "") String type, @RequestParam(value = "filter", defaultValue = "") String filter) {
        var tickets = ticketService.getTickets(authentication.getName(), page, size, status, type, filter);
        var pages = ticketService.getPages(authentication.getName(), page, size, status, type, filter);
        return ok(getResponse(request, of("tickets", tickets, "pages", pages), "Tickets retrieved", OK));
    }

    @PostMapping("/create")
    public ResponseEntity<Response> createTickets(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "title", defaultValue = "") String title, @RequestParam(value = "description", defaultValue = "") String description, @RequestParam(value = "status", defaultValue = "") String status, @RequestParam(value = "type", defaultValue = "") String type, @RequestParam(value = "priority", defaultValue = "") String priority, @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        var ticket = ticketService.createTicket(authentication.getName(), title, description, type, priority, files);
        return created(getUri()).body(getResponse(request, of("ticket", ticket), "Tickets created successfully", CREATED));
    }

    @GetMapping("/{ticketUuid}")
    public ResponseEntity<Response> getTicket(@NotNull Authentication authentication, HttpServletRequest request, @PathVariable(value = "ticketUuid") String ticketUuid) {
        var ticket = ticketService.getUserTicket(authentication.getName(), ticketUuid);
        var comments = ticketService.getTicketComments(ticketUuid);
        var files = ticketService.getTicketFiles(ticketUuid);
        var tasks = ticketService.getTicketTasks(ticketUuid);
        var assignee = userService.getAssignee(ticketUuid);
        var techSupports = userService.getTechSupports();
        var user = userService.getTicketUser(ticketUuid);
        return ok(getResponse(request, of("ticket", ticket, "comments", comments, "files", files, "tasks", tasks, "assignee", assignee, "techSupports", techSupports, "user", user), "Ticket retrieved", OK));
    }

    @PutMapping("/update")
    public ResponseEntity<Response> updateTicket(@NotNull Authentication authentication, HttpServletRequest request, @RequestBody TicketRequest ticketRequest) {
        var ticket = ticketService.updateTicket(authentication.getName(), ticketRequest.getTicketUuid(), ticketRequest.getTitle(), ticketRequest.getDescription(), ticketRequest.getProgress(), ticketRequest.getType(), ticketRequest.getPriority(), ticketRequest.getStatus(), ticketRequest.getDueDate());
        return ok(getResponse(request, of("ticket", ticket), "Ticket updated", OK));
    }

    @PutMapping("/update/assignee")
    public ResponseEntity<Response> updateAssignee(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "userUuid", defaultValue = "") String assigneeUuid, @RequestParam(value = "ticketUuid", defaultValue = "") String ticketUuid) {
        var assignee = userService.updateAssignee(authentication.getName(), assigneeUuid, ticketUuid);
        return ok(getResponse(request, of("assignee", assignee), "Ticket assigned successfully", OK));
    }

    @PostMapping("/comment/create")
    public ResponseEntity<Response> createComment(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "ticketUuid", defaultValue = "") String ticketUuid, @RequestParam(value = "comment", defaultValue = "") String comment) {
        var newComment = ticketService.createComment(authentication.getName(), ticketUuid, comment);
        return created(getUri()).body(getResponse(request, of("comment", newComment), "Comment added", CREATED));
    }

    @PutMapping("/comment/update")
    public ResponseEntity<Response> updateComment(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "commentUuid", defaultValue = "") String commentUuid, @RequestParam(value = "comment", defaultValue = "") String comment) {
        var updatedComment = ticketService.updateComment(authentication.getName(), commentUuid, comment);
        return ok(getResponse(request, of("comment", updatedComment), "Comment updated", OK));
    }

    @DeleteMapping("/comment/delete")
    public ResponseEntity<Response> deleteComment(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "commentUuid", defaultValue = "") String commentUuid) {
        ticketService.deleteComment(authentication.getName(), commentUuid);
        return ok(getResponse(request, emptyMap(), "Comment deleted", OK));
    }

    @PutMapping("/task/create")
    public ResponseEntity<Response> createTask(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "ticketUuid", defaultValue = "") String ticketUuid, @RequestParam(value = "name", defaultValue = "") String name, @RequestParam(value = "description", defaultValue = "") String description, @RequestParam(value = "status", defaultValue = "") String status) {
        var task = ticketService.createTask(authentication.getName(), ticketUuid, name, description, status);
        return ok(getResponse(request, of("task", task), "Task created", OK));
    }

    // Homework/assignment - add functionalities to update and delete a task

    @PostMapping("/file/upload")
    public ResponseEntity<Response> uploadFiles(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "ticketUuid", defaultValue = "") String ticketUuid, @RequestParam(value = "files") List<MultipartFile> files) {
        var newFiles = ticketService.uploadFiles(authentication.getName(), ticketUuid, files);
        return created(getUri()).body(getResponse(request, of("files", newFiles), "Files uploaded successfully", OK));
    }

    @DeleteMapping("/file/delete")
    public ResponseEntity<Response> deleteFile(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "fileUuid", defaultValue = "") String fileUuid) {
        ticketService.deleteFile(authentication.getName(), fileUuid);
        return ok(getResponse(request, emptyMap(), "File deleted", OK));
    }

    @GetMapping("/file/download/{fileUuid}")
    public ResponseEntity<Resource> downloadFile(@NotNull Authentication authentication, HttpServletRequest request, @PathVariable("fileUuid") String fileUuid) throws IOException {
        var path = ticketService.downloadFile(authentication.getName(), fileUuid);
        var resource = new UrlResource(path.toUri());
        var headers = new HttpHeaders();
        headers.add(FILE_NAME_HEADER, resource.getFilename());
        headers.add(CONTENT_DISPOSITION, "attachment;" + FILE_NAME_HEADER + "=" + resource.getFilename());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                .headers(headers).body(resource);
    }

    @PostMapping("/report")
    public ResponseEntity<Response> report(@NotNull Authentication authentication, HttpServletRequest request, @RequestBody ReportRequest report) {
        var tickets = ticketService.report(authentication.getName(), report.getFilter(), report.getFromDate(), report.getToDate(), report.getStatuses(), report.getTypes(), report.getPriorities());
        return ok(getResponse(request, of("tickets", tickets), "Report returned", OK));
    }

    @PostMapping(value = "/report/download", produces = { APPLICATION_PDF_VALUE })
    public ResponseEntity<ResponseEntity.BodyBuilder> exportPdf(@NotNull Authentication authentication, HttpServletResponse response, @RequestBody ReportRequest report) {
        ticketService.exportPdf(response, authentication.getName(), report.getFilter(), report.getFromDate(), report.getToDate(), report.getStatuses(), report.getTypes(), report.getPriorities());
        return ok().build();
    }

    private URI getUri() {
        return URI.create("/ticket/<ticketUuid>");
    }

}