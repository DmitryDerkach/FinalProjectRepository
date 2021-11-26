package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.MedicineDto;
import service.MedicineService;
import util.JspHelper;

@WebServlet("/final-servlet")
public class FinalServlet extends HttpServlet{

	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("submitReject").equals("1")) {
			req.getSession().setAttribute("resultRejected", "rejected");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
		} else {
			req.getSession().setAttribute("resultFine", "rejected");
			req.getSession().setAttribute("medicines", medicineService.findAll());
			req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
		}
		
	}
}
