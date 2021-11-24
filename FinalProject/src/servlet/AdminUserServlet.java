package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.UserService;
import util.JspHelper;

@WebServlet("/admin-users")
public class AdminUserServlet extends HttpServlet {
	private final UserService userService = UserService.getInstance();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("users", userService.findAll());
        var requestDispatcher = req.getRequestDispatcher(JspHelper.getPath("admin-users"));
        requestDispatcher.forward(req, resp);
        
//		resp.setContentType("text/html");
//		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
//		
//		try(PrintWriter printWriter = resp.getWriter()) {
//		printWriter.write("<h1>Список юзеров:</h1>");
//		printWriter.write("<ul>");
//		userService.findAll().forEach(userDto -> {
//			printWriter.write(String.format("<li> %d %s %s %s</li>", userDto.getId(), userDto.getName(), userDto.getEmail(), userDto.getRole()));				 
//		});
//		printWriter.write("</ul>");
//		}
		
		
	}
}
