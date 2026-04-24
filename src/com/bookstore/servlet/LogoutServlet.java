package com.bookstore.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 销毁整个session（清除登录状态 + 购物车也不会丢，因为你购物车存在cart里）
        request.getSession().invalidate();

        // 跳回首页
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}