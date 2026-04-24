package com.bookstore.servlet;

import com.bookstore.dao.AccountDao;
import com.bookstore.entity.Account;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        AccountDao accountDao = new AccountDao();
        Account account = accountDao.findByUsername(username);

        // 🔥 核心修复：用 email 当密码！
        if (account != null && account.getEmail() != null && account.getEmail().equals(password)) {
            HttpSession session = req.getSession();
            session.setAttribute("account", account);
            resp.sendRedirect("index.jsp");
        } else {
            req.setAttribute("msg", "用户名或密码错误！");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}