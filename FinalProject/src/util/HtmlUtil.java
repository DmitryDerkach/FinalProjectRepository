package util;

/* Описание (2026): добавлено в 2026. Экранирование текста для HTML, который сервлет пишет сам (без JSP):
 * /medicines, /admin?view=servlet, /admin-users?view=servlet. В JSP то же делают c:out и fn:escapeXml.
 * Без него название лекарства или имя пользователя с тегами (<script>...) выполнялось бы в браузере (XSS). */
public final class HtmlUtil {

	private HtmlUtil() {
	}

	public static String escape(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			switch (c) {
				case '<': result.append("&lt;"); break;
				case '>': result.append("&gt;"); break;
				case '&': result.append("&amp;"); break;
				case '"': result.append("&#034;"); break;
				case '\'': result.append("&#039;"); break;
				default: result.append(c);
			}
		}
		return result.toString();
	}
}
