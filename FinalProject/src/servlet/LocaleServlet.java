package servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

/* Описание (2026): смена языка (/locale, кнопки "Русский" / "English" в шапке). Язык кладётся в сессию ("lang"),
 * header.jsp передаёт его в fmt:setLocale, и fmt:message берёт тексты из translations_ru_RU.properties или
 * translations.properties. Затем redirect обратно на страницу, где нажали кнопку (заголовок Referer). */
@WebServlet("/locale")
public class LocaleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var language = req.getParameter("lang");
        // fix 2026: кнопка English отправляла en_EN - такого языка нет (английский в Англии - en_GB, в США -
        // en_US); тексты всё равно были английскими только потому, что translations.properties - запасной файл.
        // Теперь принимаются только известные языки
        if (!"ru_RU".equals(language)) {
            language = "en_US";
        }
        req.getSession().setAttribute("lang", language);
        var prevPage = req.getHeader("referer");
        // fix 2026: к адресу каждый раз дописывался "?lang=..." - после двух нажатий /login?lang=ru_RU?lang=en_US
        // (а у /login?error адрес становился неверным). Язык и так лежит в сессии, поэтому адрес не меняется.
        // Из Referer берётся только путь и параметры: redirect не уведёт на чужой сайт
        var page = prevPage != null ? pathOf(prevPage) : "/registration";
        resp.sendRedirect(page);
    }

    // 2026: "/login?error&email=a" из "http://host:port/login?error&email=a&lang=..." (старый параметр lang убран)
    private static String pathOf(String referer) {
        try {
            URI uri = URI.create(referer);
            String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            if (!path.startsWith("/") || path.startsWith("//")) {
                return "/registration";
            }
            String query = uri.getRawQuery() == null ? "" : Arrays.stream(uri.getRawQuery().split("&"))
                    .filter(part -> !part.startsWith("lang="))
                    .collect(Collectors.joining("&"));
            return query.isEmpty() ? path : path + "?" + query;
        } catch (IllegalArgumentException e) {
            return "/registration";
        }
    }
}