package servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/locale")
public class LocaleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var language = req.getParameter("lang");
        req.getSession().setAttribute("lang", language);
        var prevPage = req.getHeader("referer");
        var page = prevPage != null ? prevPage : "/registration";
        resp.sendRedirect(page + "?lang=" + language);
    }
}