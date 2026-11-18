package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.MedicineService;
import util.HtmlUtil;

/* Описание (2026): первый сервлет проекта (/medicines) - проверка слоёв DAO -> сервис -> сервлет: HTML со
 * списком лекарств пишется прямо из Java, без JSP. Открыт любому вошедшему пользователю. */
@WebServlet("/medicines")
public class MedicineServlet extends HttpServlet{

	private final MedicineService medicineService = MedicineService.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("text/html");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		try(PrintWriter printWriter = resp.getWriter()) {
		printWriter.write("<h1>Список лекарств:</h1>");
		printWriter.write("<ul>");
		medicineService.findAll().forEach(MedicineDto -> {
			// fix 2026: HtmlUtil.escape - название с тегами выполнялось бы в браузере (XSS)
			printWriter.write(String.format("<li> %s %d %s </li>", HtmlUtil.escape(MedicineDto.getName()), MedicineDto.getPrice(), HtmlUtil.escape(MedicineDto.getCountryOfProduction())));
		});
		printWriter.write("</ul>");
		}
	}

}
