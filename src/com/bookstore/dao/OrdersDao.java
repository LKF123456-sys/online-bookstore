package com.bookstore.dao;

import com.bookstore.entity.Orders;
import com.bookstore.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersDao {

    // 1. 查询所有订单
    public List<Orders> findAllOrders() {
        List<Orders> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM orders ORDER BY orderdate DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Orders order = new Orders();
                order.setOrderid(rs.getString("orderid"));
                order.setUserid(rs.getString("userid"));
                order.setOrderdate(rs.getTimestamp("orderdate"));
                order.setShipaddr1(rs.getString("shipaddr1"));
                order.setShipcity(rs.getString("shipcity"));
                order.setShipstate(rs.getString("shipstate"));
                order.setShipzip(rs.getString("shipzip"));
                order.setCourier(rs.getString("courier"));
                order.setTotalprice(rs.getBigDecimal("totalprice"));
                order.setBilltofirstname(rs.getString("billtofirstname"));
                order.setBilltolastname(rs.getString("billtolastname"));
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    // 2. 新增：插入订单方法
    public boolean insertOrder(Orders order) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO orders (orderid, userid, orderdate, totalprice, billtofirstname, billtolastname, shipaddr1, shipcity, shipstate, shipzip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, order.getOrderid());
            pstmt.setString(2, order.getUserid());
            pstmt.setTimestamp(3, new java.sql.Timestamp(order.getOrderdate().getTime()));
            pstmt.setBigDecimal(4, order.getTotalprice());
            pstmt.setString(5, order.getBilltofirstname());
            pstmt.setString(6, order.getBilltolastname());
            pstmt.setString(7, order.getShipaddr1());
            pstmt.setString(8, order.getShipcity());
            pstmt.setString(9, order.getShipstate());
            pstmt.setString(10, order.getShipzip());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // 3. 更新订单物流状态
    public boolean updateOrderStatus(String orderId, String courier) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE orders SET courier=? WHERE orderid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courier);
            pstmt.setString(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}
