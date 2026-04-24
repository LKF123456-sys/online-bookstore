package com.bookstore.dao;

import com.bookstore.entity.Account;
import com.bookstore.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDao {

    // 根据用户名查询账号（登录用）
    public Account findByUsername(String username) {
        Account account = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM account WHERE userid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                account = new Account();
                account.setUserid(rs.getString("userid"));
                // 🔥 核心：email 就是密码！
                account.setPassword(rs.getString("email"));
                account.setEmail(rs.getString("email"));
                account.setFirstname(rs.getString("firstname"));
                account.setLastname(rs.getString("lastname"));
                account.setStatus(rs.getInt("status"));
                account.setAddr1(rs.getString("addr1"));
                account.setAddr2(rs.getString("addr2"));
                account.setCity(rs.getString("city"));
                account.setState(rs.getString("state"));
                account.setZip(rs.getString("zip"));
                account.setCountry(rs.getString("country"));
                account.setPhone(rs.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return account;
    }

    // 注册账号
    public boolean register(Account account) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean flag = false;

        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO account(userid, email, firstname, lastname, status, addr1, addr2, city, state, zip, country, phone) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, account.getUserid());
            pstmt.setString(2, account.getPassword()); // 🔥 email 就是密码
            pstmt.setString(3, account.getFirstname());
            pstmt.setString(4, account.getLastname());
            pstmt.setInt(5, account.getStatus());
            pstmt.setString(6, account.getAddr1());
            pstmt.setString(7, account.getAddr2());
            pstmt.setString(8, account.getCity());
            pstmt.setString(9, account.getState());
            pstmt.setString(10, account.getZip());
            pstmt.setString(11, account.getCountry());
            pstmt.setString(12, account.getPhone());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return flag;
    }
}