package com.bookstore.promotion.service;

import com.bookstore.common.api.vo.AnnouncementVO;
import com.bookstore.common.api.vo.PageResult;

import java.util.List;

/**
 * Announcement service interface.
 * Defines all announcement-related business operations.
 */
public interface IAnnouncementService {

    /**
     * Get active announcements for users.
     * @return list of active announcements
     */
    List<AnnouncementVO> getActiveAnnouncements();

    /**
     * Get all announcements (admin, paginated).
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated announcement list
     */
    PageResult<AnnouncementVO> getAllAnnouncements(Integer pageNum, Integer pageSize);

    /**
     * Create an announcement (admin).
     * @param vo announcement data
     */
    void createAnnouncement(AnnouncementVO vo);

    /**
     * Update an announcement (admin).
     * @param id the announcement ID
     * @param vo updated announcement data
     */
    void updateAnnouncement(Long id, AnnouncementVO vo);

    /**
     * Delete an announcement (admin).
     * @param id the announcement ID
     */
    void deleteAnnouncement(Long id);

    /**
     * Update announcement status (admin).
     * @param id the announcement ID
     * @param status target status (1=enabled, 0=disabled)
     */
    void updateAnnouncementStatus(Long id, Integer status);
}
