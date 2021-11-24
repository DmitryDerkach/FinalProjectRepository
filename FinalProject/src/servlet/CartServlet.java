package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.MedicineDto;
import service.MedicineService;

@WebServlet("/cart-servlet")
public class CartServlet extends HttpServlet{

	private static final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
;		String[] quantity = req.getParameterValues("quantity");
;		String[] id = req.getParameterValues("id");
		int totalPrice = 0;
		for (int i = 0; i < id.length; i++) {
			System.out.println(id[i]);
			System.out.println(quantity[i]);
		}
		
		for (int i = 0; i< id.length; i++) {
			MedicineDto medicineDto = medicineService.findbyId(Integer.parseInt(id[i]));
			totalPrice += medicineDto.getPrice() * Integer.parseInt(quantity[i]);	
		}
		
	}
}
