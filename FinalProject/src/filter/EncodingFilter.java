package filter;

import java.nio.charset.StandardCharsets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import lombok.SneakyThrows;

/* Описание (2026): ставит UTF-8 запросу и ответу для всего сайта, до того как сервлет прочтёт параметры.
 * Без него Tomcat читает тело формы как ISO-8859-1, и "Анальгин" превращается в "ÐÐ½Ð°...".
 * Один фильтр вместо вызова setCharacterEncoding в каждом сервлете. В Spring Boot это CharacterEncodingFilter, который подключается автоматически. */
@WebFilter("/*")
public class EncodingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}
	@SneakyThrows
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
