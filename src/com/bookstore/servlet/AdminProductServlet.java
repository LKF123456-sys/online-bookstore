package com.bookstore.servlet;

import com.bookstore.dao.ProductDao;
import com.bookstore.entity.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@WebServlet("/admin/product")
@MultipartConfig
public class AdminProductServlet extends HttpServlet {

    private ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            String productId = req.getParameter("productId");
            productDao.deleteProduct(productId);
            resp.sendRedirect(req.getContextPath() + "/admin/product/list.jsp");
        } else if ("toggleStatus".equals(action)) {
            String productId = req.getParameter("productId");
            int status = Integer.parseInt(req.getParameter("status"));
            productDao.updateStatus(productId, status);
            resp.sendRedirect(req.getContextPath() + "/admin/product/list.jsp");
        } else {
            req.setAttribute("productList", productDao.findAll());
            req.getRequestDispatcher("/admin/product/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if ("add".equals(action)) {
            Product product = new Product();
            product.setProductid(req.getParameter("productid"));
            product.setCategory(req.getParameter("category"));
            product.setName(req.getParameter("name"));
            product.setDescn(req.getParameter("descn"));
            product.setPrice(new BigDecimal(req.getParameter("price")));
            product.setStatus(1);

            Part filePart = req.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                String uploadDir = getServletContext().getRealPath("/img/books");
                File uploadFile = new File(uploadDir);
                if (!uploadFile.exists()) uploadFile.mkdirs();

                String newFileName = product.getProductid() + "." + getExtension(fileName);
                filePart.write(uploadDir + File.separator + newFileName);
                product.setImage("img/books/" + newFileName);
            }

            productDao.addProduct(product);
        } else if ("edit".equals(action)) {
            Product product = new Product();
            product.setProductid(req.getParameter("productid"));
            product.setCategory(req.getParameter("category"));
            product.setName(req.getParameter("name"));
            product.setDescn(req.getParameter("descn"));
            product.setPrice(new BigDecimal(req.getParameter("price")));
            product.setStatus(Integer.parseInt(req.getParameter("status")));

            Part filePart = req.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                String uploadDir = getServletContext().getRealPath("/img/books");
                File uploadFile = new File(uploadDir);
                if (!uploadFile.exists()) uploadFile.mkdirs();

                String newFileName = product.getProductid() + "." + getExtension(fileName);
                filePart.write(uploadDir + File.separator + newFileName);
                product.setImage("img/books/" + newFileName);
            }

            productDao.updateProduct(product);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/product/list.jsp");
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private String getExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        return "jpg";
    }
}
