package notificationservice.repository;

import notificationservice.model.Message;

import java.util.List;

/**
 * @author Junior RT
 * @version 1.0
 * @license Get Arrays, LLC (<a href="https://www.getarrays.io">Get Arrays, LLC</a>)
 * @email getarrayz@gmail.com
 * @since 1/22/25
 */

public interface NotificationRepository {
    Message sendMessage(String fromUserUuid, String toEmail, String subject, String message);
    List<Message> getMessages(String userUuid);
    List<Message> getConversations(String userUuid, String conversationId);
    String getMessageStatus(String userUuid, Long messageId);
    String updateMessageStatus(String userUuid, Long messageId, String status);
}