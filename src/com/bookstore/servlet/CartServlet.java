package com.bookstore.servlet;

import com.bookstore.dao.ProductDao;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 1. 获取session，拿到购物车
        HttpSession session = req.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        // 2. 获取操作方式
        String method = req.getParameter("method");

        if ("add".equals(method)) {
            // 3. 添加商品到购物车
            String productId = req.getParameter("productId");
            ProductDao productDao = new ProductDao();
            Product product = productDao.findById(productId);

            if (product != null) {
                cart.addItem(new CartItem(product, 1));
            }

            resp.getWriter().write("success");
            return; // 直接结束，不往下走

        } else if ("remove".equals(method)) {
            // 4. 删除商品
            String productId = req.getParameter("productId");
            cart.removeItem(productId);
        } else if ("clear".equals(method)) {
            // 5. 清空购物车
            cart.clear();
        }

        // 6. 只有 删除/清空 才跳转到购物车页面
        //    添加商品不会走到这里
        req.getRequestDispatcher("/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}