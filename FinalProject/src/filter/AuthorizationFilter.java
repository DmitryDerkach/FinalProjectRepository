package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dto.UserDto;
import entity.Role;

import java.io.IOException;
import java.util.Set;

/* Описание (2026): охранник всего сайта (@WebFilter("/*") - каждый запрос проходит через него до сервлета).
 * Открытые страницы (вход, регистрация, смена языка) пропускает всегда; остальные - только если в сессии
 * есть "user" (его кладёт LoginServlet), иначе redirect на /login. 2026: страницы /admin... - только для
 * роли ADMIN. То же самое в Spring делает Spring Security (SecurityFilterChain: permitAll / authenticated /
 * hasRole("ADMIN")) - тоже фильтр, стоящий перед DispatcherServlet. */
@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    private static final Set<String> PUBLIC_PATH = Set.of("/login", "/registration", "/locale");
    // 2026: стартовая страница демо (index.html) и логотип в шапке тоже открыты - точное совпадение,
    // а не startsWith: с "/" начинается любой адрес
    private static final Set<String> START_PAGE = Set.of("/", "/index.html");
    private static final String IMAGES = "/images/";
    // 2026: страницы администратора - /admin, /admin-users, /admin-medicines
    private static final String ADMIN_PATH = "/admin";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var uri = ((HttpServletRequest) request).getRequestURI();
        if (isPublicPath(uri) || isUserLoggedIn(request)) {
            // fix 2026: роль не проверялась - обычный пользователь открывал /admin-users и удалял лекарства
            // через /admin-medicines. Теперь USER на страницах администратора отправляется к своим лекарствам
            if (uri.startsWith(ADMIN_PATH) && !isAdmin(request)) {
                ((HttpServletResponse) response).sendRedirect("/user-medicines");
                return;
            }
            chain.doFilter(request, response);
        } else {
            // fix 2026: "referer" читался из ответа (response.getHeader) - там его нет, значение всегда null,
            // и аноним всегда попадал на /login. Так и оставлено: на страницу входа, без чтения заголовка
            ((HttpServletResponse)response).sendRedirect("/login");
        }
    }

    private boolean isUserLoggedIn(ServletRequest servletRequest) {
        var user = (UserDto) ((HttpServletRequest) servletRequest).getSession().getAttribute("user");
        return user != null;
    }

    // 2026: для страниц /admin...
    private boolean isAdmin(ServletRequest servletRequest) {
        var user = (UserDto) ((HttpServletRequest) servletRequest).getSession().getAttribute("user");
        return user != null && user.getRole() == Role.ADMIN;
    }

	private boolean isPublicPath(String uri) {
        return PUBLIC_PATH.stream().anyMatch(path -> uri.startsWith(path))
                || START_PAGE.contains(uri) || uri.startsWith(IMAGES);
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}


}