package servlet;

import dto.MedicineDto;
import service.MedicineService;
import util.JspHelper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/user-cart")
public class UserMedicineCartServlet extends HttpServlet {

	private final MedicineService medicineService = MedicineService.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String[] parameterValues = req.getParameterValues("medicineId");
        String[] arrayQuantity = req.getParameterValues("quantity");

        ArrayList<String> quantity = new ArrayList<>();

        for (String s : arrayQuantity) {
            if (!s.equals("0")) {
                quantity.add(s);
            }
        }

        if (parameterValues.length== quantity.size()) {

            ArrayList<Integer> totalPriceEachMedicine = new ArrayList<>();

            int totalPrice = 0;

            List<MedicineDto> medicineDtos = new ArrayList<>();
            for (String s : parameterValues) {
                medicineDtos.add(medicineService.findbyId(Integer.parseInt(s)));
            }

            int t;
            for (int i = 0; i < quantity.size(); i++) {
                t = Integer.parseInt(quantity.get(i)) * medicineDtos.get(i).getPrice();
                totalPriceEachMedicine.add(t);
                totalPrice+=t;
            }

            req.getSession().setAttribute("medicineDtos" , medicineDtos);
            req.getSession().setAttribute("quantity", quantity);
            req.getSession().setAttribute("totalPriceEachMedicine", totalPriceEachMedicine);
            req.getSession().setAttribute("totalPrice", totalPrice);
            req.getRequestDispatcher(JspHelper.getPath("user-medicine-cart")).forward(req, resp);
        } else {
            req.getSession().setAttribute("quantityError", "1");
            resp.sendRedirect("http://localhost:8080/user-medicines");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(JspHelper.getPath("user-medicine-cart")).forward(req, resp);
    }
}