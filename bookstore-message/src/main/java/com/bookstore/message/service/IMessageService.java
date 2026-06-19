package com.bookstore.message.service;

import com.bookstore.common.api.vo.MessageVO;
import com.bookstore.common.api.vo.PageResult;

/**
 * Message service interface.
 * Defines all message-related business operations.
 */
public interface IMessageService {

    /**
     * Get paginated message list for a user.
     * @param receiverId the receiver's user ID
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated message list
     */
    PageResult<MessageVO> getMessageList(String receiverId, Integer pageNum, Integer pageSize);

    /**
     * Mark a single message as read.
     * @param receiverId the receiver's user ID
     * @param messageId the message ID
     */
    void markAsRead(String receiverId, Long messageId);

    /**
     * Mark all messages as read for a user.
     * @param receiverId the receiver's user ID
     */
    void markAllAsRead(String receiverId);

    /**
     * Get unread message count for a user.
     * @param receiverId the receiver's user ID
     * @return unread count
     */
    Long getUnreadCount(String receiverId);

    /**
     * Send a point-to-point message.
     * @param senderId sender ID
     * @param senderType sender type
     * @param receiverId receiver ID
     * @param receiverType receiver type
     * @param content message content
     */
    void sendMessage(String senderId, String senderType, String receiverId, String receiverType, String content);

    /**
     * Get all messages (admin, paginated).
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated message list
     */
    PageResult<MessageVO> getAllMessages(Integer pageNum, Integer pageSize);

    /**
     * Send a system broadcast message to all users.
     * @param content broadcast content
     */
    void sendBroadcast(String content);
}
