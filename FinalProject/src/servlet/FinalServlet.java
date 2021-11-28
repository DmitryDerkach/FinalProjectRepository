package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MedicineService;

@WebServlet("/final-servlet")
public class FinalServlet extends HttpServlet{

	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("submitReject").equals("1")) {
			req.setAttribute("request", "Покупка была отменена!");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			//req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
			resp.sendRedirect("http://localhost:8080/user-medicines");
		} else {
			req.getSession().setAttribute("request", "Покупка была совершена!");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			//req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
			resp.sendRedirect("http://localhost:8080/user-medicines");
		}
		
	}
}
