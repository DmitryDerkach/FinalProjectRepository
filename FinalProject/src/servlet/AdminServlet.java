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
import util.JspHelper;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
	
	private final MedicineService medicineService = MedicineService.getInstance();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var requestDispatcher = req.getRequestDispatcher(JspHelper.getPath("admin"));
        requestDispatcher.forward(req, resp);
//		resp.setContentType("text/html");
//		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
//		
//		try(PrintWriter printWriter = resp.getWriter()) {
//		printWriter.write("<h1>Список лекарств:</h1>");
//		printWriter.write("<ul>");
//		medicineService.findAll().forEach(MedicineDto -> {
//			printWriter.write(String.format("<li> %s %d %s </li>", MedicineDto.getName(), MedicineDto.getPrice(), MedicineDto.getCountryOfProduction()));				 
//		});
//		printWriter.write("</ul>");
//		}
	}

}
