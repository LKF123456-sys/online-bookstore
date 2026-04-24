package com.bookstore.servlet;

import com.bookstore.dao.AnnouncementDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/announcement")
public class AdminAnnouncementServlet extends HttpServlet {

    private AnnouncementDao announcementDao = new AnnouncementDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            announcementDao.delete(id);
        }

        req.setAttribute("announcementList", announcementDao.findAll());
        req.getRequestDispatcher("/admin/announcement/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        String title = req.getParameter("title");
        String content = req.getParameter("content");

        if ("add".equals(action)) {
            announcementDao.add(title, content);
        } else if ("edit".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            announcementDao.update(id, title, content);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/announcement/list.jsp");
    }
}
