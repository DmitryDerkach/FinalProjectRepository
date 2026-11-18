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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/* Описание (2026): корзина (/user-cart). POST приходит с формы user-medicines.jsp: отмеченные лекарства
 * (medicineId) и количество в каждой строке (quantity). Сервлет считает стоимость каждого лекарства и итог,
 * кладёт корзину в сессию и показывает user-medicine-cart.jsp. Неверное количество - назад к списку
 * с сообщением об ошибке (quantityError). */
@WebServlet("/user-cart")
public class UserMedicineCartServlet extends HttpServlet {

	private final MedicineService medicineService = MedicineService.getInstance();
	// 2026: верхняя граница количества одного лекарства - иначе цена * количество переполняет int
	private static final int MAX_QUANTITY = 1000;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        String[] parameterValues = req.getParameterValues("medicineId");
        String[] arrayQuantity = req.getParameterValues("quantity");
        // 2026: id лекарства каждой строки формы (скрытое поле в user-medicines.jsp), в том же порядке, что quantity
        String[] rowIds = req.getParameterValues("rowId");

        // fix 2026: количество сопоставлялось с лекарством по порядку среди ненулевых полей, а не по строке:
        // отметил Ibuprofen, а число вписал в строку Validol - покупался Ibuprofen в этом количестве.
        // Теперь у каждого поля количества есть id его строки, и для отмеченного лекарства берётся число из
        // его же строки. Без отмеченных лекарств (NullPointerException), с текстом или пустым полем
        // (NumberFormatException), с отрицательным количеством (итог -70$) - теперь ошибка формы, а не 500
        ArrayList<String> quantity = new ArrayList<>();
        List<String> selectedIds = new ArrayList<>();
        boolean valid = parameterValues != null && arrayQuantity != null && rowIds != null
                && rowIds.length == arrayQuantity.length;
        if (valid) {
            List<String> checked = Arrays.asList(parameterValues);
            for (int i = 0; i < rowIds.length; i++) {
                String s = arrayQuantity[i].trim();
                if (checked.contains(rowIds[i])) {
                    Integer number = parseNumber(s);
                    if (number == null || number < 1 || number > MAX_QUANTITY) {
                        valid = false;
                    } else {
                        quantity.add(String.valueOf(number));
                        selectedIds.add(rowIds[i]);
                    }
                } else if (!s.isEmpty() && !s.equals("0")) {
                    // количество в строке без галочки - как и в 2021, это ошибка ввода
                    valid = false;
                }
            }
        }

        List<MedicineDto> medicineDtos = new ArrayList<>();
        if (valid) {
            for (String s : selectedIds) {
                Optional<MedicineDto> medicine = medicineService.findById(Integer.parseInt(s));
                if (medicine.isEmpty()) {
                    // 2026: лекарство удалили, пока страница была открыта (или id подделан)
                    valid = false;
                    break;
                }
                medicineDtos.add(medicine.get());
            }
        }

        if (valid && parameterValues.length == quantity.size()) {

            ArrayList<Integer> totalPriceEachMedicine = new ArrayList<>();

            int totalPrice = 0;

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
            // fix 2026: адрес был зашит как http://localhost:8080/user-medicines - на другом порту (или сервере)
            // после ошибки открывалось чужое приложение. Теперь путь внутри этого приложения
            resp.sendRedirect(req.getContextPath() + "/user-medicines");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // fix 2026: без корзины в сессии (открыли /user-cart по ссылке, после покупки) JSP падал:
        // c:forEach с end = -1 ("'end' < 0", ошибка 500). Теперь - назад к списку лекарств
        Object cart = req.getSession().getAttribute("medicineDtos");
        if (!(cart instanceof List) || ((List<?>) cart).isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/user-medicines");
            return;
        }
        req.getRequestDispatcher(JspHelper.getPath("user-medicine-cart")).forward(req, resp);
    }

    // 2026: число из формы или null, если это не целое число
    private static Integer parseNumber(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}