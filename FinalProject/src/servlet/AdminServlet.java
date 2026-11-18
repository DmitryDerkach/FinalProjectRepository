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
import util.JspHelper;

/* Описание (2026): главная страница администратора (/admin) - две кнопки: пользователи и лекарства. Сюда
 * LoginServlet отправляет ADMIN после входа. 2026: /admin?view=servlet - закомментированная первая версия,
 * где сервлет сам писал HTML со списком лекарств (до JSP). */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

	private final MedicineService medicineService = MedicineService.getInstance();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 2026: закомментированный код снова работает - /admin?view=servlet
		if ("servlet".equals(req.getParameter("view"))) {
			writeHtmlByHand(resp);
			return;
		}
        var requestDispatcher = req.getRequestDispatcher(JspHelper.getPath("admin"));
        requestDispatcher.forward(req, resp);
	}

	private void writeHtmlByHand(HttpServletResponse resp) throws IOException {
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
