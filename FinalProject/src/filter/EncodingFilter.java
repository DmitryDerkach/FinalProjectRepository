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
