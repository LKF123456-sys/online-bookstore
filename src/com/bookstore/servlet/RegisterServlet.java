package com.bookstore.servlet;

import com.bookstore.dao.AccountDao;
import com.bookstore.entity.Account;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registerServlet")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 1. 获取注册表单参数（完全匹配account表字段）
        String userid = req.getParameter("userid");
        String email = req.getParameter("email");
        String firstname = req.getParameter("firstname");
        String lastname = req.getParameter("lastname");
        String password = req.getParameter("password");
        String addr1 = req.getParameter("addr1");
        String city = req.getParameter("city");
        String state = req.getParameter("state");
        String zip = req.getParameter("zip");
        String country = req.getParameter("country");
        String phone = req.getParameter("phone");

        // 2. 封装Account对象
        Account account = new Account();
        account.setUserid(userid);
        account.setEmail(email);
        account.setFirstname(firstname);
        account.setLastname(lastname);
        account.setPassword(password); // 注意：account表需要password字段，若没有需补充
        account.setAddr1(addr1);
        account.setCity(city);
        account.setState(state);
        account.setZip(zip);
        account.setCountry(country);
        account.setPhone(phone);
        account.setStatus(1); // 默认状态为正常

        // 3. 调用DAO注册
        AccountDao accountDao = new AccountDao();
        boolean isSuccess = accountDao.register(account);

        // 4. 结果处理
        if (isSuccess) {
            // 注册成功，跳转到登录页
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        } else {
            // 注册失败，返回注册页
            req.setAttribute("errorMsg", "用户名已存在！");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}