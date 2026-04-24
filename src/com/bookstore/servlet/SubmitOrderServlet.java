package com.bookstore.servlet;

import com.bookstore.dao.OrdersDao;
import com.bookstore.entity.Account;
import com.bookstore.entity.Cart;
import com.bookstore.entity.Orders;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@WebServlet("/submitOrder")
public class SubmitOrderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processOrder(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processOrder(req, resp);
    }

    private void processOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        Account account = (Account) session.getAttribute("account");

        if (cart == null || cart.getItems().isEmpty()) {
            resp.sendRedirect("cart.jsp");
            return;
        }

        if (account == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        Orders order = new Orders();
        order.setOrderid(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserid(account.getUserid());
        order.setOrderdate(new Date());
        order.setTotalprice(cart.getTotal());

        // 填充地址信息（如果用户信息不完整，使用默认值防止报错）
        order.setBilltofirstname(account.getFirstname() != null ? account.getFirstname() : "User");
        order.setBilltolastname(account.getLastname() != null ? account.getLastname() : "");
        order.setShipaddr1(account.getAddr1() != null ? account.getAddr1() : "Default Address");
        order.setShipcity(account.getCity() != null ? account.getCity() : "Beijing");
        order.setShipstate(account.getState() != null ? account.getState() : "");
        order.setShipzip(account.getZip() != null ? account.getZip() : "100000");

        OrdersDao ordersDao = new OrdersDao();
        if (ordersDao.insertOrder(order)) {
            cart.clear();
            resp.sendRedirect("index.jsp?msg=success");
        } else {
            resp.sendRedirect("order.jsp?msg=error");
        }
    }
}
