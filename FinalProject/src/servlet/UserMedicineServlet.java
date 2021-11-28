package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MedicineService;
import util.JspHelper;

@WebServlet("/user-medicines")
public class UserMedicineServlet extends HttpServlet{
	
	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("request", "request");
		req.getSession().setAttribute("medicines", medicineService.findAll());
		req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
	}
}
