package com.bookstore.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.vo.AnnouncementVO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.entity.Announcement;
import com.bookstore.promotion.mapper.AnnouncementMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementMapper announcementMapper;

    @InjectMocks
    private AnnouncementService announcementService;

    private Announcement sampleAnnouncement;

    @BeforeEach
    void setUp() {
        sampleAnnouncement = new Announcement();
        sampleAnnouncement.setId(1L);
        sampleAnnouncement.setTitle("618大促活动");
        sampleAnnouncement.setContent("全场图书满100减20，满200减50");
        sampleAnnouncement.setStatus(1);
        sampleAnnouncement.setCreatedAt(LocalDateTime.now());
    }

    // ==================== 获取激活状态公告测试 ====================

    @Nested
    @DisplayName("获取激活状态公告")
    class GetActiveAnnouncementsTests {

        @Test
        @DisplayName("查询成功 — 返回状态为启用的公告列表")
        void shouldReturnActiveAnnouncements() {
            when(announcementMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleAnnouncement));

            List<AnnouncementVO> result = announcementService.getActiveAnnouncements();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("618大促活动", result.get(0).getTitle());
            verify(announcementMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 无激活公告时返回空列表")
        void shouldReturnEmptyWhenNoActiveAnnouncements() {
            when(announcementMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<AnnouncementVO> result = announcementService.getActiveAnnouncements();

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    // ==================== 管理员获取所有公告测试 ====================

    @Nested
    @DisplayName("管理员获取所有公告")
    class GetAllAnnouncementsTests {

        @Test
        @DisplayName("查询成功 — 返回分页的公告列表（所有状态）")
        void shouldReturnAllAnnouncements() {
            Page<Announcement> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleAnnouncement));
            page.setTotal(1);

            when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<AnnouncementVO> result = announcementService.getAllAnnouncements(1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            verify(announcementMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 无公告时返回空分页列表")
        void shouldReturnEmptyWhenNoAnnouncements() {
            Page<Announcement> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(announcementMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<AnnouncementVO> result = announcementService.getAllAnnouncements(1, 10);

            assertNotNull(result);
            assertEquals(0, result.getRecords().size());
            assertEquals(0L, result.getTotal());
        }
    }

    // ==================== 管理员创建公告测试 ====================

    @Nested
    @DisplayName("管理员创建公告")
    class CreateAnnouncementTests {

        @Test
        @DisplayName("创建成功 — 正常创建公告并保存到数据库")
        void shouldCreateAnnouncementSuccessfully() {
            AnnouncementVO vo = new AnnouncementVO();
            vo.setTitle("新书上架通知");
            vo.setContent("本周上新100本图书");
            vo.setStatus(1);

            when(announcementMapper.insert(any(Announcement.class))).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.createAnnouncement(vo));

            ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
            verify(announcementMapper).insert(captor.capture());
            Announcement inserted = captor.getValue();
            assertEquals("新书上架通知", inserted.getTitle());
            assertEquals("本周上新100本图书", inserted.getContent());
        }
    }

    // ==================== 管理员更新公告测试 ====================

    @Nested
    @DisplayName("管理员更新公告")
    class UpdateAnnouncementTests {

        @Test
        @DisplayName("更新成功 — 正常更新公告信息，ID不被覆盖")
        void shouldUpdateAnnouncementAndPreserveId() {
            AnnouncementVO vo = new AnnouncementVO();
            vo.setId(999L); // VO中传入不同的ID
            vo.setTitle("618大促活动（更新）");
            vo.setContent("更新后的内容");
            vo.setStatus(1);

            when(announcementMapper.selectById(1L)).thenReturn(sampleAnnouncement);
            when(announcementMapper.updateById(any(Announcement.class))).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.updateAnnouncement(1L, vo));

            // 验证ID被保留为原始ID（1L），不被VO中的999L覆盖
            ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
            verify(announcementMapper).updateById(captor.capture());
            Announcement updated = captor.getValue();
            assertEquals(1L, updated.getId());
        }

        @Test
        @DisplayName("更新成功 — 验证更新后的公告内容正确")
        void shouldUpdateAnnouncementContentCorrectly() {
            AnnouncementVO vo = new AnnouncementVO();
            vo.setTitle("更新标题");
            vo.setContent("更新内容");
            vo.setStatus(0);

            when(announcementMapper.selectById(1L)).thenReturn(sampleAnnouncement);
            when(announcementMapper.updateById(any(Announcement.class))).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.updateAnnouncement(1L, vo));

            verify(announcementMapper).selectById(1L);
            verify(announcementMapper).updateById(any(Announcement.class));
        }

        @Test
        @DisplayName("更新失败 — 公告不存在时抛出异常")
        void shouldThrowWhenAnnouncementNotFound() {
            AnnouncementVO vo = new AnnouncementVO();
            when(announcementMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> announcementService.updateAnnouncement(999L, vo));
            assertTrue(ex.getMessage().contains("公告不存在"));
        }
    }

    // ==================== 管理员删除公告测试 ====================

    @Nested
    @DisplayName("管理员删除公告")
    class DeleteAnnouncementTests {

        @Test
        @DisplayName("删除成功 — 物理删除公告记录")
        void shouldDeleteAnnouncementSuccessfully() {
            when(announcementMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.deleteAnnouncement(1L));

            verify(announcementMapper).deleteById(1L);
        }
    }

    // ==================== 管理员更新公告状态测试 ====================

    @Nested
    @DisplayName("管理员更新公告状态")
    class UpdateAnnouncementStatusTests {

        @Test
        @DisplayName("启用成功 — 公告状态设为已发布")
        void shouldEnableAnnouncementSuccessfully() {
            sampleAnnouncement.setStatus(0);
            when(announcementMapper.selectById(1L)).thenReturn(sampleAnnouncement);
            when(announcementMapper.updateById(any(Announcement.class))).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.updateAnnouncementStatus(1L, 1));

            assertEquals(1, sampleAnnouncement.getStatus());
            verify(announcementMapper).updateById(sampleAnnouncement);
        }

        @Test
        @DisplayName("禁用成功 — 公告状态设为草稿")
        void shouldDisableAnnouncementSuccessfully() {
            when(announcementMapper.selectById(1L)).thenReturn(sampleAnnouncement);
            when(announcementMapper.updateById(any(Announcement.class))).thenReturn(1);

            assertDoesNotThrow(() -> announcementService.updateAnnouncementStatus(1L, 0));

            assertEquals(0, sampleAnnouncement.getStatus());
            verify(announcementMapper).updateById(sampleAnnouncement);
        }

        @Test
        @DisplayName("更新失败 — 公告不存在时抛出异常")
        void shouldThrowWhenAnnouncementNotFound() {
            when(announcementMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> announcementService.updateAnnouncementStatus(999L, 1));
            assertTrue(ex.getMessage().contains("公告不存在"));
        }
    }
}
