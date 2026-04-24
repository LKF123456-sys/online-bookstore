package com.bookstore.servlet;

import com.bookstore.dao.OrdersDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/order")
public class AdminOrderServlet extends HttpServlet {

    private OrdersDao ordersDao = new OrdersDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("orderList", ordersDao.findAllOrders());
        req.getRequestDispatcher("/admin/order/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String orderId = req.getParameter("orderId");
        String courier = req.getParameter("courier");

        ordersDao.updateOrderStatus(orderId, courier);

        resp.sendRedirect(req.getContextPath() + "/admin/order/list.jsp");
    }
}
