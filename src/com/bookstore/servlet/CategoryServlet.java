package com.bookstore.servlet;

import com.bookstore.dao.ProductDao;
import com.bookstore.entity.Product;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@WebServlet("/category")
public class CategoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 设置编码（和你之前的完全一致）
        req.setCharacterEncoding("UTF-8");

        // 2. 获取点击的分类ID（从 index.jsp 传过来的，完全保留你之前的链接格式）
        String categoryId = req.getParameter("categoryId");

        // 3. 如果没传参数，默认展示科学类 C001
        if (categoryId == null || categoryId.isEmpty()) {
            categoryId = "C001";
        }

        // 4. 根据分类ID查询图书（ProductDao 是你之前的，完全复用）
        ProductDao productDao = new ProductDao();
        List<Product> productList = productDao.findByCategory(categoryId);

        // 5. 把数据和分类ID传到 index.jsp（这里是核心，把数据给到你之前的界面）
        req.setAttribute("productList", productList);
        req.setAttribute("currentCategory", categoryId); // 用于高亮当前分类

        // 6. 转发到 index.jsp（完全还原你之前的页面跳转）
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
