package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Описание (2026): выход (/logout, кнопка в шапке, только POST): invalidate() удаляет сессию со всем
 * содержимым (пользователь, корзина, язык), и следующий запрос AuthorizationFilter отправит на /login. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession().invalidate();
		resp.sendRedirect("/login");

	}
}
