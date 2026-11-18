package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MedicineService;

/* Описание (2026): последний шаг покупки (/final-servlet): кнопки корзины "Отменить покупку" (submitReject=1)
 * и "Подтвердить покупку" (submitReject=2). Сообщение для списка лекарств кладётся в сессию, затем
 * redirect на /user-medicines (Post/Redirect/Get: обновление страницы не отправит форму второй раз).
 * Покупка нигде не сохраняется - таблицы заказов в проекте нет (README, "Known issues"). */
@WebServlet("/final-servlet")
public class FinalServlet extends HttpServlet{

	private static final MedicineService medicineService = MedicineService.getInstance();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// fix 2026: без параметра submitReject было getParameter(...).equals - NullPointerException (500);
		// без корзины в сессии "покупка" проходила без товаров. Теперь просто возврат к списку лекарств
		String submitReject = req.getParameter("submitReject");
		if (submitReject == null || req.getSession().getAttribute("medicineDtos") == null) {
			resp.sendRedirect(req.getContextPath() + "/user-medicines");
			return;
		}
		if (submitReject.equals("1")) {
			// fix 2026: сообщение клалось в атрибут запроса (req.setAttribute), а после redirect это уже другой
			// запрос - "Покупка была отменена" не показывалась никогда. Теперь в сессию, как и во второй ветке
			req.getSession().setAttribute("request", "Покупка была отменена!");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			//req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
		} else {
			req.getSession().setAttribute("request", "Покупка была совершена!");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			//req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
		}
		// 2026: корзина закончена - убираем её из сессии (иначе /user-cart показывал старую корзину)
		req.getSession().removeAttribute("medicineDtos");
		req.getSession().removeAttribute("quantity");
		req.getSession().removeAttribute("totalPriceEachMedicine");
		req.getSession().removeAttribute("totalPrice");
		// fix 2026: был зашит http://localhost:8080/user-medicines (в обеих ветках) - на другом порту открывалось
		// чужое приложение. Теперь путь внутри этого приложения
		resp.sendRedirect(req.getContextPath() + "/user-medicines");

	}
}
