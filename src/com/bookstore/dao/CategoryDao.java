package com.bookstore.dao;

import com.bookstore.entity.Category;
import com.bookstore.utils.DBUtil;
import java.sql.*;
import java.util.*;

public class CategoryDao {
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM category";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryid(rs.getString("categoryid"));
                c.setName(rs.getString("name"));
                c.setDescn(rs.getString("descn"));
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
}