package com.bookstore.dao;

import com.bookstore.entity.Product;
import com.bookstore.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM product";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductid(rs.getString("productid"));
                p.setCategory(rs.getString("category"));
                p.setName(rs.getString("name"));
                p.setDescn(rs.getString("descn"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setImage(rs.getString("image"));
                p.setStatus(rs.getInt("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    public List<Product> findByCategory(String categoryId) {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM product WHERE category=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, categoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductid(rs.getString("productid"));
                p.setCategory(rs.getString("category"));
                p.setName(rs.getString("name"));
                p.setDescn(rs.getString("descn"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setImage(rs.getString("image"));
                p.setStatus(rs.getInt("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    public Product findById(String productId) {
        Product product = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM product WHERE productid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                product = new Product();
                product.setProductid(rs.getString("productid"));
                product.setCategory(rs.getString("category"));
                product.setName(rs.getString("name"));
                product.setDescn(rs.getString("descn"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImage(rs.getString("image"));
                product.setStatus(rs.getInt("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return product;
    }

    public List<Product> search(String keyword) {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM product WHERE name LIKE ? OR descn LIKE ?";
            pstmt = conn.prepareStatement(sql);
            String key = "%" + keyword + "%";
            pstmt.setString(1, key);
            pstmt.setString(2, key);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductid(rs.getString("productid"));
                p.setCategory(rs.getString("category"));
                p.setName(rs.getString("name"));
                p.setDescn(rs.getString("descn"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setImage(rs.getString("image"));
                p.setStatus(rs.getInt("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    public boolean addProduct(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO product (productid, category, name, descn, price, image, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductid());
            pstmt.setString(2, product.getCategory());
            pstmt.setString(3, product.getName());
            pstmt.setString(4, product.getDescn());
            pstmt.setBigDecimal(5, product.getPrice());
            pstmt.setString(6, product.getImage());
            pstmt.setInt(7, product.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public boolean updateProduct(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE product SET category=?, name=?, descn=?, price=?, image=?, status=? WHERE productid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getCategory());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getDescn());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setString(5, product.getImage());
            pstmt.setInt(6, product.getStatus());
            pstmt.setString(7, product.getProductid());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public boolean deleteProduct(String productId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM product WHERE productid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public boolean updateStatus(String productId, int status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE product SET status=? WHERE productid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setString(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}
