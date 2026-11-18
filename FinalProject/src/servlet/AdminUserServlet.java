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
import util.HtmlUtil;
import util.JspHelper;

/* Описание (2026): список пользователей для администратора (/admin-users): данные в атрибут запроса "users",
 * затем forward в admin-users.jsp. 2026: /admin-users?view=servlet - закомментированная первая версия,
 * где сервлет сам писал HTML. */
@WebServlet("/admin-users")
public class AdminUserServlet extends HttpServlet {
	private final UserService userService = UserService.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 2026: закомментированный код снова работает - /admin-users?view=servlet
		if ("servlet".equals(req.getParameter("view"))) {
			writeHtmlByHand(resp);
			return;
		}
		req.setAttribute("users", userService.findAll());
        var requestDispatcher = req.getRequestDispatcher(JspHelper.getPath("admin-users"));
        requestDispatcher.forward(req, resp);
	}

	private void writeHtmlByHand(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		try(PrintWriter printWriter = resp.getWriter()) {
		printWriter.write("<h1>Список юзеров:</h1>");
		printWriter.write("<ul>");
		userService.findAll().forEach(userDto -> {
			// fix 2026: HtmlUtil.escape - имя с тегами выполнялось бы в браузере (XSS)
			printWriter.write(String.format("<li> %d %s %s %s</li>", userDto.getId(), HtmlUtil.escape(userDto.getName()), HtmlUtil.escape(userDto.getEmail()), userDto.getRole()));
		});
		printWriter.write("</ul>");
		}


	}
}
