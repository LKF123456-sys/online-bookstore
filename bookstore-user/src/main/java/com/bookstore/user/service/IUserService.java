package com.bookstore.user.service;

import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.PasswordUpdateDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.UserVO;

import java.util.Map;

/**
 * User account service interface.
 * Defines all user-related business operations.
 */
public interface IUserService {

    /**
     * Authenticate user and return JWT token with user info.
     * @param dto login credentials
     * @return map containing token and user info
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * Logout by blacklisting the current token.
     * @param token the JWT token to revoke
     */
    void logout(String token);

    /**
     * Register a new user account.
     * @param dto registration details
     */
    void register(RegisterDTO dto);

    /**
     * Get user info by ID.
     * @param id the user ID
     * @return user view object
     */
    UserVO getUserById(String id);

    /**
     * Update user password.
     * @param userId the user ID
     * @param dto old and new password
     */
    void updatePassword(String userId, PasswordUpdateDTO dto);

    /**
     * Update user profile (email, phone, avatar, etc.).
     * @param userId the user ID
     * @param vo updated profile data
     */
    void updateProfile(String userId, UserVO vo);

    /**
     * Get paginated user list (admin).
     * @param pageNum page number
     * @param pageSize page size
     * @param keyword optional search keyword
     * @return paginated user list
     */
    PageResult<UserVO> getUserList(Integer pageNum, Integer pageSize, String keyword);

    /**
     * Update user status (admin).
     * @param userId the user ID
     * @param status target status (1=enabled, 0=disabled)
     */
    void updateUserStatus(String userId, Integer status);

    /**
     * Delete a user (admin).
     * @param userId the user ID
     */
    void deleteUser(String userId);
}
