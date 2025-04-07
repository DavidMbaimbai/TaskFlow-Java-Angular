package ticketservice.service.implementation;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ticketservice.exception.ApiException;
import ticketservice.model.Attachment;
import ticketservice.model.Comment;
import ticketservice.model.Task;
import ticketservice.model.Ticket;
import ticketservice.repository.TicketRepository;
import ticketservice.service.TicketService;
import ticketservice.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.util.StringUtils.cleanPath;
import static ticketservice.constant.Constants.FILE_NAME_HEADER;
import static ticketservice.constant.Constants.PHOTO_DIRECTORY;
import static ticketservice.utils.DateFormatter.*;
import static ticketservice.utils.TicketUtils.getFileUri;
import static ticketservice.utils.UserUtils.hasElevatedPermissions;


@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private ApplicationEventPublisher publisher;
    @Value("${ui.app.url}")
    private String uiAppUrl;

    @Override
    public List<Ticket> getTickets(String userUuid, int page, int size, String status, String type, String filter) {
        var user = userService.getUserByUuid(userUuid);
        return hasElevatedPermissions.apply(user) ?  ticketRepository.getTickets(userUuid, page, size, status, type, filter) : ticketRepository.getUserTickets(userUuid, page, size, status, type, filter);
    }

    @Override
    public int getPages(String userUuid, int page, int size, String status, String type, String filter) {
        var user = userService.getUserByUuid(userUuid);
        return hasElevatedPermissions.apply(user) ? ticketRepository.getPages(userUuid, page, size, status, type, filter) : ticketRepository.getUserPages(userUuid, page, size, status, type, filter);
    }

    @Override
    public Ticket createTicket(String userUuid, String title, String description, String type, String priority, List<MultipartFile> files) {
        var ticket = ticketRepository.createTicket(userUuid, title, description, type, priority);
        if(null != files && !files.isEmpty()) {
            for (var file : files) {
                ticketRepository.saveTicketFile(ticket.getTicketId(), file.getOriginalFilename(), file.getSize(), byteCountToDisplaySize(file.getSize()), getExtension(file.getOriginalFilename()), getFileUri(file.getOriginalFilename()));
            }
            ticket.setFileCount(files.size());
        }
        // Save files on AWS S2/Disk
        var user = userService.getUserByUuid(userUuid);
        //publisher.publishEvent(new Event(TICKET_CREATED, Map.of("priority", ticket.getPriority(), "ticketTitle", ticket.getTitle(), "ticketNumber", ticket.getTicketUuid(), "name", capitalizeFully(user.getFirstName()), "email", user.getEmail())));
        return ticket;
    }

    @Override
    public Ticket getUserTicket(String userUuid, String ticketUuid) {
        var user = userService.getUserByUuid(userUuid);
        return hasElevatedPermissions.apply(user) ?  ticketRepository.getTicket(userUuid, ticketUuid) : ticketRepository.getUserTicket(userUuid, ticketUuid);
    }

    @Override
    public List<Comment> getTicketComments(String ticketUuid) {
        return ticketRepository.getTicketComments(ticketUuid);
    }

    @Override
    public List<Task> getTicketTasks(String ticketUuid) {
        return ticketRepository.getTicketTasks(ticketUuid);
    }

    @Override
    public Comment createComment(String userUuid, String ticketUuid, String comment) {
        var ticket = ticketRepository.getTicket(userUuid, ticketUuid);
        var newComment = ticketRepository.createComment(userUuid, ticketUuid, comment);
        var user = userService.getUserByUuid(userUuid);
        if(!Objects.equals(user.getUserUuid(), userUuid)) {
            //publisher.publishEvent(new Event(COMMENT_CREATED, of("date", shortDate(ticket.getCreatedAt()), "priority", ticket.getPriority()), "ticketTitle", ticket.getTitle(), "comment", newComment.getComment(), "ticketNumber", ticket.getTicketUuid()), capitalizeFully(user.getFirstName()), "email", user.getEmail());
        }
        return newComment;
    }

    @Override
    public List<Attachment> getTicketFiles(String ticketUuid) {
        return ticketRepository.getTicketFiles(ticketUuid);
    }

    @Override
    public List<Attachment> uploadFiles(String userUuid, String ticketUuid, List<MultipartFile> files) {
        try {
            var fileList = new ArrayList<Attachment>();
            Ticket ticket;
            if(null != files && !files.isEmpty()) {
                var user = userService.getUserByUuid(userUuid);
                if(hasElevatedPermissions.apply(user)) {
                    ticket = ticketRepository.getTicket(userUuid, ticketUuid);
                } else {
                    ticket = ticketRepository.getUserTicket(userUuid, ticketUuid);
                }
                for(var file : files) {
                    var attachment = ticketRepository.saveTicketFile(ticket.getTicketId(), file.getOriginalFilename(), file.getSize(), byteCountToDisplaySize(file.getSize()), getExtension(file.getOriginalFilename()), getFileUri(file.getOriginalFilename()));
                    var filename = cleanPath(file.getOriginalFilename());
                    var fileStorage = get(PHOTO_DIRECTORY, filename).toAbsolutePath().normalize();
                    copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
                    fileList.add(attachment);
                }
                var ticketUser = userService.getTicketUser(ticketUuid);
                if(!Objects.equals(userUuid, ticketUser.getUserUuid())) {
                    var filenames = fileList.stream().map(Attachment::getName).collect(Collectors.joining(", "));
                    //publisher.publishEvent(new Event(FILE_UPLOADED, of("date", shortDate(ticket.getCreatedAt()), "priority", ticket.getPriority(), "ticketTitle", ticket.getTitle(), "files", filenames, "ticketNumber", ticket.getTicketUuid(), "name", capitalizeFully(user.getFirstName()), "email", user.getEmail())));
                }
            }
            return fileList;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again later.");
        }
    }

    @Override
    public void deleteFile(String userUuid, String fileUuid) {
        ticketRepository.deleteFile(userUuid, fileUuid);
    }

    @Override
    public Path downloadFile(String userUuid, String fileUuid) {
        try {
            var attachment = ticketRepository.getTicketFile(userUuid, fileUuid);
            var filePath = get(PHOTO_DIRECTORY).toAbsolutePath().normalize().resolve(attachment.getName());
            if(!Files.exists(filePath)) {
                throw new ApiException(String.format("File %s not found on the server", attachment.getName()));
            }
            return filePath;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again later.");
        }
    }

    @Override
    public Comment updateComment(String userUuid, String commentUuid, String comment) {
        return ticketRepository.updateComment(userUuid, commentUuid, comment);
    }

    @Override
    public void deleteComment(String userUuid, String commentUuid) {
        ticketRepository.deleteComment(userUuid, commentUuid);
    }

    @Override
    public Ticket updateTicket(String userUuid, String ticketUuid, String title, String description, int progress, String type, String priority, String status, String dueDate) {
        return ticketRepository.updateTicket(userUuid, ticketUuid, title, description, progress, type, priority, status, dueDate);
    }

    @Override
    public Task createTask(String userUuid, String ticketUuid, String name, String description, String status) {
        return ticketRepository.createTask(userUuid, ticketUuid, name, description, status);
    }

    @Override
    public Attachment getTicketFile(String userUuid, String fileUuid) {
        return ticketRepository.getTicketFile(userUuid, fileUuid);
    }

    @Override
    public List<Ticket> report(String userUuid, String filter, String fromDate, String toDate, List<String> statuses, List<String> types, List<String> priorities) {
        var user = userService.getUserByUuid(userUuid);
        return hasElevatedPermissions.apply(user) ? ticketRepository.report(filter, fromDate, toDate, statuses, types, priorities) : ticketRepository.report(userUuid, filter, fromDate, toDate, statuses, types, priorities);
    }

    @Override
    public void exportPdf(HttpServletResponse response, String userUuid, String filter, String fromDate, String toDate, List<String> statuses, List<String> types, List<String> priorities) {
        try {
            var user = userService.getUserByUuid(userUuid);
            var tickets = hasElevatedPermissions.apply(user) ? ticketRepository.report(filter, fromDate, toDate, statuses, types, priorities) : ticketRepository.report(userUuid, filter, fromDate, toDate, statuses, types, priorities);
            PdfWriter pdfWriter = new PdfWriter(response.getOutputStream());
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDocument, PageSize.A4, false);
            String imFile = "/Users/david/Downloads/catsupport.png";
            ImageData data = ImageDataFactory.create(imFile);
            Image image = new Image(data);
            image.scaleToFit(40, 40);
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(image);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            document.add(new Paragraph("CAT SUPPORT TICKET REPORT").setFontSize(20).setFontColor(new DeviceRgb(46, 134, 193)).setFont(bold).setBackgroundColor(new DeviceRgb(229, 228, 226)).setTextAlignment(TextAlignment.CENTER));
            Table nestedTable = new Table(new float[]{1, 1});
            nestedTable.setMarginTop(10f);
            nestedTable.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Created by:").setFont(bold).setFontSize(14))).setBorder(Border.NO_BORDER);
            nestedTable.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph(user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")").setFont(regular).setFontSize(14)));

            Table nestedTable1 = new Table(new float[]{1, 1});
            //nestedTable1.addCell(new Cell().setBorderRadius(new BorderRadius(5)).setBackgroundColor(new DeviceRgb(127, 15, 183)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Report Date:").setFont(bold).setFontColor(new DeviceRgb(255, 255, 255)).setFontSize(14))).setBorder(Border.NO_BORDER);
            nestedTable1.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Report Date:").setFont(bold).setFontSize(14))).setBorder(Border.NO_BORDER);
            nestedTable1.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph(formattedDate()).setFont(regular).setFontSize(14)));
            Table nestedTable2 = new Table(new float[]{1, 1});
            nestedTable2.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Total tickets:").setFont(bold).setFontSize(14))).setBorder(Border.NO_BORDER);
            nestedTable2.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph(String.valueOf(tickets.size())).setFont(regular).setFontSize(14)));

            Border border = new SolidBorder(new DeviceRgb(229, 228, 226), 2);
            Table divider = new Table(new float[]{0.5f});
            divider.setMarginTop(15f);
            divider.setBorder(border);
            divider.setWidth(UnitValue.createPercentValue(100));

            Table ticketsTable = new Table(new float[]{1, 1, 1, 1, 1, 1, 1});
            ticketsTable.setWidth(UnitValue.createPercentValue(100));
            ticketsTable.setMarginTop(20f);
            tableHeader(ticketsTable);

            addTickets(tickets, ticketsTable);


           /* document.add(new Paragraph("Created by: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")").setFont(bold).setTextAlignment(TextAlignment.JUSTIFIED));
            document.add(new Paragraph("Date: " + new Date())
                    .setFont(regular).setTextAlignment(TextAlignment.RIGHT));*/

            float[]columnWidths = {285, 285+150};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            /*Table nestedTable1 = new Table(new float[]{1, 1});
            nestedTable1.setWidth(UnitValue.createPercentValue(100));
            nestedTable1.addCell(new Cell().setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Cat Support:").setFont(bold).setFontSize(14)));
            nestedTable1.addCell(new Cell().setTextAlignment(TextAlignment.RIGHT).add(new Paragraph("Tickets Report").setFont(regular).setFontSize(14)));*/

            Cell cell = new Cell(1, 3)
                    .add(new Paragraph("Tickets Report"))
                    //.setFont(f)
                    .setFontSize(12)
                    .setFontColor(DeviceGray.BLACK)
                    //.setBackgroundColor(DeviceGray.GRAY)
                    //.setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            Cell cell2 = new Cell(1, 3)
                    .add(new Paragraph("Date: " + new Date()))
                    .setFont(bold)
                    .setFontSize(12)
                    .setFontColor(DeviceGray.BLACK)
                    .setBackgroundColor(DeviceGray.GRAY)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            //table.addHeaderCell(cell);
            //table.addCell(new Cell().add(nestedTable1).add(nestedTable2));
            //table.addCell(new Cell().add(nestedTable2));
            //table.addCell(cell2);
            //document.add(table);
            document.add(nestedTable);
            document.add(nestedTable1);
            document.add(nestedTable2);
            //document.add(divider);
            document.add(ticketsTable);
            int numberOfPages = pdfDocument.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                document.showTextAligned(new Paragraph(String.format("Page %s of %s", i, numberOfPages)).setFont(bold).setFontSize(10), 559, 10, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
            }
            String reportFilename = today("yyyy-MM-dd-hh:mm:ss") + "-report.pdf";
            response.setHeader(FILE_NAME_HEADER, reportFilename);
            response.setHeader(CONTENT_DISPOSITION, "attachment;File-Name=" + reportFilename);
            document.flush();
            document.close();
        } catch (Exception exception) {
            throw new ApiException(exception.getMessage());
        }
    }

    private void addTickets(List<Ticket> tickets, Table table) {
        tickets.forEach(ticket -> {
            table.addCell(getCell(ticket.getTicketUuid()).setFontSize(8));
            table.addCell(getCell(ticket.getTitle()).setFontSize(8));
            //table.addCell(getCell(ticket.getDescription()));
            table.addCell(getCell(shortDate(ticket.getCreatedAt())).setFontSize(8));
            table.addCell(getCell(shortDate(ticket.getDueDate())).setFontSize(8));
            table.addCell(getCell(ticket.getStatus()).setFontSize(8));
            table.addCell(getCell(ticket.getType()).setFontSize(8));
            table.addCell(getCell(ticket.getPriority()).setFontSize(8));
        });
    }

    private void tableHeader(Table table) {
        Stream.of("Ticket #", "Title", "Created", "Due Date", "Status", "Type", "Priority").forEach(header -> {
            try {
                var cell = new Cell(1, 1)
                        .add(new Paragraph(header))
                        .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
                        .setFontSize(12)
                        .setFontColor(DeviceGray.BLACK)
                        .setBackgroundColor(new DeviceRgb(229, 228, 226))
                        //.setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Cell getCell(String value) {
        try {
            return new Cell(1, 1)
                    .add(new Paragraph(value))
                    .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                    .setFontSize(12)
                    //.setBackgroundColor(new DeviceRgb(115, 198, 182))
                    //.setFontColor(new DeviceRgb(115, 198, 182))
                    .setFontColor(DeviceGray.BLACK)
                    //.setBackgroundColor(DeviceGray.GRAY)
                    //.setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}