package servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.UserDto;
import lombok.SneakyThrows;
import service.UserService;
import util.JspHelper;

/* Описание (2026): вход (/login). GET - форма login.jsp. POST - UserService ищет пользователя по email и паролю:
 * найден - UserDto кладётся в сессию ("user") и redirect по роли (ADMIN -> /admin, USER -> /user-medicines);
 * не найден - redirect на /login?error. Сессию Tomcat узнаёт по cookie JSESSIONID, так что после входа
 * каждый следующий запрос этого браузера проходит AuthorizationFilter. */
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
        // fix 2026: читался параметр "email", а поле формы называется "eMail" - в адресе всегда было email=null.
        // + URLEncoder: email с "&" или "#" ломал адрес. login.jsp подставляет email обратно в поле
        String email = req.getParameter("eMail");
        resp.sendRedirect("/login?error&email=" + URLEncoder.encode(email == null ? "" : email, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private void onLoginSuccess(UserDto user, HttpServletRequest req, HttpServletResponse resp) {
        // fix 2026: новый id сессии после входа (защита от session fixation: id, известный до входа,
        // например подсунутый в ссылке, не становится id сессии вошедшего пользователя)
        req.getSession();
        req.changeSessionId();
        req.getSession().setAttribute("user", user);
        if (user.getRole().toString().equals("ADMIN")) {
        	resp.sendRedirect("/admin");
        } else {
        	 resp.sendRedirect("/user-medicines");
        }

    }


}
