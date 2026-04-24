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

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 设置编码
        req.setCharacterEncoding("UTF-8");

        // 2. 获取搜索关键词
        String keyword = req.getParameter("keyword");

        // 3. 如果关键词为空，默认展示所有图书
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }

        // 4. 调用 ProductDao 的 search 方法
        ProductDao productDao = new ProductDao();
        List<Product> searchResult = productDao.search(keyword);

        // 5. 把搜索结果传到 index.jsp
        req.setAttribute("productList", searchResult);
        req.setAttribute("searchKeyword", keyword);

        // 6. 转发到 index.jsp 展示结果
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}