package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.MedicineDto;
import service.MedicineService;
import util.JspHelper;

@WebServlet("/user-medicines")
public class UserMedicineServlet extends HttpServlet{
	
	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession().setAttribute("medicines", medicineService.findAll());
		req.getRequestDispatcher(JspHelper.getPath("user-medicines")).forward(req, resp);
	}
	
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parameterValues = req.getParameterValues("medicineId");
        String[] quantity =  req.getParameterValues("quantity");
        List<MedicineDto> medicineDtoContainer = new ArrayList<>();
        for (String s : parameterValues) {
        	medicineDtoContainer.add(medicineService.findbyId(Integer.parseInt(s)));
        }
        req.getSession().setAttribute("medicines", medicineDtoContainer);
		req.getRequestDispatcher(JspHelper.getPath("user-medicine-cart")).forward(req, resp);
    }

}
