package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.UserDto;
import lombok.SneakyThrows;
import service.UserService;
import util.JspHelper;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private final UserService userService = UserService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var requestDispatcher = req.getRequestDispatcher(JspHelper.getPath("login"));
        requestDispatcher.forward(req, resp);

    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userService.login(req.getParameter("eMail"), req.getParameter("password"))
                .ifPresentOrElse(
                        user -> onLoginSuccess(user, req, resp),
                        () -> onLoginFail(req, resp)
                );
    }
    
    @SneakyThrows
    private void onLoginFail(HttpServletRequest req, HttpServletResponse resp) {
        resp.sendRedirect("/login?error&email=" + req.getParameter("email"));
    }

    @SneakyThrows
    private void onLoginSuccess(UserDto user, HttpServletRequest req, HttpServletResponse resp) {
        req.getSession().setAttribute("user", user);
        if (user.getRole().toString().equals("ADMIN")) {
        	resp.sendRedirect("/admin");
        } else {
        	 resp.sendRedirect("/user-medicines");
        }
       
    }
    
    
}
