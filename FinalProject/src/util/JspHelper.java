package util;

import lombok.experimental.UtilityClass;

/* Описание (2026): путь к JSP по короткому имени: "login" -> /WEB-INF/jsp/login.jsp. JSP лежат в WEB-INF,
 * поэтому браузер не может открыть их напрямую - только через сервлет (forward). */
@UtilityClass
public class JspHelper {

	private static final String JSP_FORMAT = "/WEB-INF/jsp/%s.jsp";

	public static String getPath(String jspName) {
		return String.format(JSP_FORMAT, jspName);
	}
}