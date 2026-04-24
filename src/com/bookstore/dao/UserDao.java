package com.bookstore.dao;

import com.bookstore.entity.User;
import com.bookstore.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    /**
     * 登录验证方法
     * @param userid 用户名
     * @param password 密码
     * @return 成功返回User对象，失败返回null
     */
    public User login(String userid, String password) {
        User user = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 1. 获取连接
            conn = DBUtil.getConnection();
            // 2. 编写SQL
            String sql = "SELECT * FROM account WHERE userid = ? AND password = ?";
            // 3. 预处理SQL
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userid);
            pstmt.setString(2, password);
            // 4. 执行查询
            rs = pstmt.executeQuery();
            // 5. 处理结果
            if (rs.next()) {
                user = new User();
                // 把数据库查到的数据封装到User对象中
                user.setUserid(rs.getString("userid"));
                user.setPassword(rs.getString("password"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setAddr1(rs.getString("addr1"));
                user.setCity(rs.getString("city"));
                user.setState(rs.getString("state"));
                user.setZip(rs.getString("zip"));
                user.setPhone(rs.getString("phone"));
                user.setStatus(rs.getInt("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6. 关闭资源
            DBUtil.close(conn, pstmt, rs);
        }
        return user;
    }
}