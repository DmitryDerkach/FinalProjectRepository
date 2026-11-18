package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dto.UserDto;
import entity.Role;
import exception.ValidationException;
import service.UserService;
import util.JspHelper;

/* Описание (2026): регистрация (/registration). GET - форма registration.jsp, POST - новый пользователь
 * с ролью USER (администратора через сайт создать нельзя) и redirect на /login. */
@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet{

	private final UserService userService = UserService.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher(JspHelper.getPath("registration"))
		.forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		UserDto userDto = UserDto.builder()
				.name(req.getParameter("userName"))
				.email(req.getParameter("eMail"))
				.password(req.getParameter("password"))
				.role(Role.USER)
				.build();
		try {
			userService.create(userDto);
			// 2026: ?registered - login.jsp показывает "регистрация прошла успешно"
			resp.sendRedirect("/login?registered");
		} catch (ValidationException e) {
			// fix 2026: ошибки формы (пустые поля, неверный email, email уже занят) - на той же странице,
			// введённое остаётся в полях. Раньше это была ошибка 500 или сохранение пустого пользователя
			req.setAttribute("errors", e.getErrors());
			doGet(req, resp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
