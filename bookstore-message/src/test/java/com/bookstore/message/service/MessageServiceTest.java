package com.bookstore.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.vo.MessageVO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.entity.Message;
import com.bookstore.message.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 * 覆盖 MessageService 所有公开方法的正常路径和边界场景
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    private Message sampleMessage;
    private Message sampleMessage2;

    @BeforeEach
    void setUp() {
        sampleMessage = new Message();
        sampleMessage.setId(1L);
        sampleMessage.setSenderId("user001");
        sampleMessage.setSenderType("user");
        sampleMessage.setReceiverId("user002");
        sampleMessage.setReceiverType("user");
        sampleMessage.setContent("你好，这是一条测试消息");
        sampleMessage.setReadStatus(0);
        sampleMessage.setCreateTime(LocalDateTime.of(2026, 6, 10, 14, 30));

        sampleMessage2 = new Message();
        sampleMessage2.setId(2L);
        sampleMessage2.setSenderId("admin001");
        sampleMessage2.setSenderType("admin");
        sampleMessage2.setReceiverId("user002");
        sampleMessage2.setReceiverType("user");
        sampleMessage2.setContent("您的订单已发货");
        sampleMessage2.setReadStatus(1);
        sampleMessage2.setCreateTime(LocalDateTime.of(2026, 6, 11, 9, 0));
    }

    // ==================== 获取消息列表测试 ====================

    @Nested
    @DisplayName("获取用户消息列表 getMessageList")
    class GetMessageListTests {

        @Test
        @DisplayName("查询成功 — 正常返回分页消息列表，按创建时间倒序")
        void shouldReturnPaginatedMessagesOrderedByCreateTimeDesc() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(sampleMessage2, sampleMessage));
            page.setTotal(2);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<MessageVO> result = messageService.getMessageList("user002", 1, 10);

            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            assertEquals(2, result.getRecords().size());
            // 验证第一条是较新的消息（倒序）
            assertEquals("您的订单已发货", result.getRecords().get(0).getContent());
            assertEquals("你好，这是一条测试消息", result.getRecords().get(1).getContent());

            verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 无消息时返回空列表")
        void shouldReturnEmptyListWhenNoMessages() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<MessageVO> result = messageService.getMessageList("user999", 1, 10);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("分页参数 — 第二页查询正确传递分页信息")
        void shouldPassCorrectPaginationParams() {
            Page<Message> page = new Page<>(2, 5);
            page.setRecords(List.of(sampleMessage));
            page.setTotal(6);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<MessageVO> result = messageService.getMessageList("user002", 2, 5);

            assertNotNull(result);
            assertEquals(6, result.getTotal());
            assertEquals(2, result.getPageNum());
            assertEquals(5, result.getPageSize());
            assertEquals(1, result.getRecords().size());
        }
    }

    // ==================== 标记单条消息已读测试 ====================

    @Nested
    @DisplayName("标记单条消息已读 markAsRead")
    class MarkAsReadTests {

        @Test
        @DisplayName("标记成功 — 未读消息变为已读")
        void shouldMarkMessageAsReadSuccessfully() {
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleMessage);
            when(messageMapper.updateById(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() -> messageService.markAsRead("user002", 1L));

            assertEquals(1, sampleMessage.getReadStatus());
            verify(messageMapper).updateById(sampleMessage);
        }

        @Test
        @DisplayName("标记失败 — 消息不存在时抛出 IllegalArgumentException")
        void shouldThrowWhenMessageNotFound() {
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> messageService.markAsRead("user002", 999L));
            assertEquals("消息不存在", ex.getMessage());

            verify(messageMapper, never()).updateById(any(Message.class));
        }

        @Test
        @DisplayName("标记失败 — 消息属于其他用户时抛出 IllegalArgumentException")
        void shouldThrowWhenMessageBelongsToAnotherUser() {
            // user003 尝试标记 receiverId=user002 的消息，selectOne 返回 null
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> messageService.markAsRead("user003", 1L));
            assertEquals("消息不存在", ex.getMessage());

            verify(messageMapper, never()).updateById(any(Message.class));
        }

        @Test
        @DisplayName("标记成功 — 已读消息重复标记不报错（幂等）")
        void shouldNotThrowWhenMarkingAlreadyReadMessage() {
            sampleMessage.setReadStatus(1);
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleMessage);
            when(messageMapper.updateById(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() -> messageService.markAsRead("user002", 1L));

            assertEquals(1, sampleMessage.getReadStatus());
            verify(messageMapper).updateById(sampleMessage);
        }
    }

    // ==================== 批量标记全部已读测试 ====================

    @Nested
    @DisplayName("批量标记所有消息已读 markAllAsRead")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("批量标记成功 — 调用 update 将未读消息全部置为已读")
        void shouldMarkAllMessagesAsRead() {
            when(messageMapper.update(any(Message.class), any(LambdaQueryWrapper.class))).thenReturn(3);

            assertDoesNotThrow(() -> messageService.markAllAsRead("user002"));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).update(captor.capture(), any(LambdaQueryWrapper.class));
            assertEquals(1, captor.getValue().getReadStatus());
        }

        @Test
        @DisplayName("批量标记 — 没有未读消息时不抛异常（update 影响 0 行）")
        void shouldNotThrowWhenNoUnreadMessages() {
            when(messageMapper.update(any(Message.class), any(LambdaQueryWrapper.class))).thenReturn(0);

            assertDoesNotThrow(() -> messageService.markAllAsRead("user999"));

            verify(messageMapper).update(any(Message.class), any(LambdaQueryWrapper.class));
        }
    }

    // ==================== 获取未读消息数量测试 ====================

    @Nested
    @DisplayName("获取未读消息数量 getUnreadCount")
    class GetUnreadCountTests {

        @Test
        @DisplayName("存在未读消息 — 返回正确数量")
        void shouldReturnCorrectUnreadCount() {
            when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            Long count = messageService.getUnreadCount("user002");

            assertEquals(5L, count);
            verify(messageMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无未读消息 — 返回 0")
        void shouldReturnZeroWhenNoUnreadMessages() {
            when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            Long count = messageService.getUnreadCount("user002");

            assertEquals(0L, count);
        }
    }

    // ==================== 发送消息测试 ====================

    @Nested
    @DisplayName("发送消息 sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("发送成功 — 消息字段正确设置，readStatus 为 0")
        void shouldSendMessageWithCorrectFields() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() ->
                    messageService.sendMessage("user001", "user", "user002", "user", "你好"));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).insert(captor.capture());

            Message saved = captor.getValue();
            assertEquals("user001", saved.getSenderId());
            assertEquals("user", saved.getSenderType());
            assertEquals("user002", saved.getReceiverId());
            assertEquals("user", saved.getReceiverType());
            assertEquals("你好", saved.getContent());
            assertEquals(0, saved.getReadStatus());
        }

        @Test
        @DisplayName("发送成功 — 管理员给用户发消息，字段类型正确")
        void shouldSendAdminMessageWithCorrectTypes() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() ->
                    messageService.sendMessage("admin001", "admin", "user002", "user", "系统通知：维护公告"));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).insert(captor.capture());

            Message saved = captor.getValue();
            assertEquals("admin001", saved.getSenderId());
            assertEquals("admin", saved.getSenderType());
            assertEquals("user002", saved.getReceiverId());
            assertEquals("user", saved.getReceiverType());
            assertEquals("系统通知：维护公告", saved.getContent());
            assertEquals(0, saved.getReadStatus());
        }

        @Test
        @DisplayName("发送成功 — 空内容消息也能正常插入")
        void shouldSendMessageWithEmptyContent() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() ->
                    messageService.sendMessage("user001", "user", "user002", "user", ""));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).insert(captor.capture());
            assertEquals("", captor.getValue().getContent());
        }
    }

    // ==================== 管理员查看所有消息测试 ====================

    @Nested
    @DisplayName("管理员查看所有消息 getAllMessages")
    class GetAllMessagesTests {

        @Test
        @DisplayName("查询成功 — 返回所有用户的消息，按创建时间倒序")
        void shouldReturnAllMessagesOrderedByCreateTimeDesc() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(sampleMessage2, sampleMessage));
            page.setTotal(2);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<MessageVO> result = messageService.getAllMessages(1, 10);

            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            assertEquals(2, result.getRecords().size());

            // 验证 VO 字段完整映射
            MessageVO first = result.getRecords().get(0);
            assertEquals(sampleMessage2.getId(), first.getId());
            assertEquals("admin001", first.getSenderId());
            assertEquals("admin", first.getSenderType());
            assertEquals("user002", first.getReceiverId());
            assertEquals("您的订单已发货", first.getContent());
            assertEquals(1, first.getReadStatus());

            verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 系统中无消息时返回空列表")
        void shouldReturnEmptyListWhenNoMessagesExist() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<MessageVO> result = messageService.getAllMessages(1, 10);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }

    // ==================== 发送系统广播测试 ====================

    @Nested
    @DisplayName("发送系统广播 sendBroadcast")
    class SendBroadcastTests {

        @Test
        @DisplayName("广播成功 — senderId=0, senderType=system, receiverId=all, receiverType=all")
        void shouldSendBroadcastWithCorrectSystemFields() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() -> messageService.sendBroadcast("系统维护通知：今晚 22:00 停机维护"));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).insert(captor.capture());

            Message saved = captor.getValue();
            assertEquals("0", saved.getSenderId());
            assertEquals("system", saved.getSenderType());
            assertEquals("all", saved.getReceiverId());
            assertEquals("all", saved.getReceiverType());
            assertEquals("系统维护通知：今晚 22:00 停机维护", saved.getContent());
            assertEquals(0, saved.getReadStatus());
        }

        @Test
        @DisplayName("广播成功 — 空内容广播也能正常发送")
        void shouldSendBroadcastWithEmptyContent() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            assertDoesNotThrow(() -> messageService.sendBroadcast(""));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageMapper).insert(captor.capture());
            assertEquals("", captor.getValue().getContent());
            assertEquals("0", captor.getValue().getSenderId());
            assertEquals("system", captor.getValue().getSenderType());
        }
    }
}
