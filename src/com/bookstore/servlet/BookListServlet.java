package com.bookstore.servlet;

import com.bookstore.dao.ProductDao;
import com.bookstore.entity.Product;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/bookList")
public class BookListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 1. 调用DAO查询
        ProductDao productDao = new ProductDao();
        List<Product> bookList = productDao.findAll();

        // 2. 打印日志：看控制台有没有数据（排查用）
        System.out.println("查询到的图书数量：" + bookList.size());

        // 3. 存入request域
        req.setAttribute("bookList", bookList);

        // 4. 转发到JSP
        req.getRequestDispatcher("/bookList.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}