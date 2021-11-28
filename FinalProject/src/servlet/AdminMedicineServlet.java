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

@WebServlet("/admin-medicines")
public class AdminMedicineServlet extends HttpServlet {
	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession().setAttribute("medicines", medicineService.findAll());
		req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);
	}
	
/*<%--sessionScope--%>*/	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("medicineAddition") != null) {
			MedicineDto medicineDto = MedicineDto.builder()
					.name(req.getParameter("name"))
					.countryOfProduction(req.getParameter("countryOfProduction"))
					.price(Integer.parseInt(req.getParameter("price")))
					.build();
				String result = medicineService.save(medicineDto);
				req.getSession().setAttribute("result", result);
				req.getSession().setAttribute("medicines", medicineService.findAll());
				req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);
		} else {
			MedicineDto medicineDto = MedicineDto.builder()
					.id(Integer.parseInt(req.getParameter("id")))
					.name(req.getParameter("name"))
					.countryOfProduction(req.getParameter("country"))
					.price(Integer.parseInt(req.getParameter("price")))
					.build();
			String result = medicineService.delete(medicineDto);
			req.getSession().setAttribute("result", result);
			req.getSession().setAttribute("medicines", medicineService.findAll());
			req.getRequestDispatcher(JspHelper.getPath("admin-medicines")).forward(req, resp);

		}	
	}
}
