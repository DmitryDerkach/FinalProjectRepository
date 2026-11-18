package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dto.MedicineDto;
import service.MedicineService;
import util.JspHelper;

/* Описание (2026): склад лекарств для администратора (/admin-medicines). GET - список и форма добавления,
 * POST - добавление (скрытое поле medicineAddition) или удаление (кнопка у лекарства, его id в скрытом поле).
 * Результат ("Saved", "Successfully deleted" ...) кладётся в сессию, admin-medicines.jsp показывает его
 * переведённым. Доступ только у ADMIN - это проверяет AuthorizationFilter, а не сам сервлет. */
@WebServlet("/admin-medicines")
public class AdminMedicineServlet extends HttpServlet {
	private static final MedicineService medicineService = MedicineService.getInstance();
	// 2026: столько же, сколько колонки name и country_of_production в db/01_schema.sql
	private static final int MAX_LENGTH = 128;
	private static final int MAX_PRICE = 1_000_000;

	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// fix 2026: результат прошлого действия оставался в сессии и снова показывался при каждом открытии страницы
		req.getSession().removeAttribute("result");
		req.getSession().setAttribute("medicines", medicineService.findAll());
		req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);
	}

/*<%--sessionScope--%>*/

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("medicineAddition") != null) {
			// fix 2026: цена "abc", пустая или без поля давала NumberFormatException (ошибка 500), а пустые название
			// и страна сохранялись (браузер проверяет required, но запрос можно отправить и без браузера)
			Integer price = parseNumber(req.getParameter("price"));
			String name = req.getParameter("name");
			String country = req.getParameter("countryOfProduction");
			if (isBlankOrTooLong(name) || isBlankOrTooLong(country) || price == null || price < 0 || price > MAX_PRICE) {
				req.getSession().setAttribute("result", "Invalid medicine");
			} else {
			MedicineDto medicineDto = MedicineDto.builder()
					.name(name.trim())
					.countryOfProduction(country.trim())
					.price(price)
					.build();
				String result = medicineService.save(medicineDto);
				req.getSession().setAttribute("result", result);
			}
				req.getSession().setAttribute("medicines", medicineService.findAll());
				req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);
		} else {
			// fix 2026: без id (или с нечисловым id) удаление падало с NumberFormatException; для удаления нужен
			// только id - цена из скрытого поля больше не разбирается строго (без неё тоже было 500)
			Integer id = parseNumber(req.getParameter("id"));
			if (id == null) {
				req.getSession().setAttribute("result", "Was not deleted");
			} else {
			Integer price = parseNumber(req.getParameter("price"));
			MedicineDto medicineDto = MedicineDto.builder()
					.id(id)
					.name(req.getParameter("name"))
					.countryOfProduction(req.getParameter("country"))
					.price(price == null ? 0 : price)
					.build();
			String result = medicineService.delete(medicineDto);
			req.getSession().setAttribute("result", result);
			}
			req.getSession().setAttribute("medicines", medicineService.findAll());
			req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);

		}
	}

	// 2026: число из формы или null, если это не целое число
	private static Integer parseNumber(String value) {
		try {
			return value == null ? null : Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean isBlankOrTooLong(String value) {
		return value == null || value.isBlank() || value.trim().length() > MAX_LENGTH;
	}
}
