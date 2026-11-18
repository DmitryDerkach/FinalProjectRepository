package servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.FileResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import dao.MedicineDao;
import dao.UserDao;
import entity.Medicine;
import entity.User;

/* Описание (2026): всё веб-приложение на настоящем Tomcat 9 - встроенном (embedded), том же, что Spring Boot
 * запускает внутри своего приложения. Сайт собирается как WAR: WebContent - корень (index.html, images,
 * WEB-INF/jsp, web.xml), target/classes - WEB-INF/classes, jstl-1.2.jar - WEB-INF/lib (из него Jasper берёт
 * теги c:, fmt:, fn:). Фильтры (@WebFilter) и JSP работают по-настоящему. База - та же pharmacy_database
 * (порт 5435), ./run.sh test поднимает и сбрасывает её. Каждый Browser - отдельный "браузер" со своей
 * cookie JSESSIONID, то есть со своей сессией. */
@Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WebAppTest {

	private static Tomcat tomcat;
	private static String baseUrl;

	@BeforeAll
	static void startTomcat() throws Exception {
		tomcat = new Tomcat();
		tomcat.setBaseDir(Files.createDirectories(Path.of("target", "tomcat")).toString());
		tomcat.setPort(0); // any free port
		tomcat.getConnector();
		Context context = tomcat.addWebapp("", new File("WebContent").getAbsolutePath());
		WebResourceRoot resources = new StandardRoot(context);
		resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
				new File("target/classes").getAbsolutePath(), "/"));
		// JSTL as in the WAR: a jar in WEB-INF/lib, where Jasper looks for tag libraries (TLD files)
		File jstl = new File(javax.servlet.jsp.jstl.core.Config.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		resources.addPreResources(new FileResourceSet(resources, "/WEB-INF/lib/jstl-1.2.jar", jstl.getAbsolutePath(), "/"));
		context.setResources(resources);
		// every test run starts without the sessions of the previous one (Tomcat saves them to SESSIONS.ser on stop)
		StandardManager manager = new StandardManager();
		manager.setPathname(null);
		context.setManager(manager);
		// the test classpath has many jars (JUnit, Tomcat itself): don't scan them for TLDs and annotations
		((StandardJarScanner) context.getJarScanner()).setScanClassPath(false);
		tomcat.start();
		baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort();
	}

	@AfterAll
	static void stopTomcat() throws Exception {
		tomcat.stop();
		tomcat.destroy();
	}

	// ---------- a small browser: one session cookie, no redirects followed (the tests look at them) ----------

	private static final class Browser {
		private final HttpClient client = HttpClient.newHttpClient();
		private String session = "";

		HttpResponse<String> get(String path, String... headers) throws Exception {
			return send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET(), headers);
		}

		/** A form as a browser sends it: application/x-www-form-urlencoded, "name", "value", "name", "value"... */
		HttpResponse<String> post(String path, String... fields) throws Exception {
			return postWithHeaders(path, new String[0], fields);
		}

		HttpResponse<String> postWithHeaders(String path, String[] headers, String... fields) throws Exception {
			StringBuilder body = new StringBuilder();
			for (int i = 0; i < fields.length; i += 2) {
				if (body.length() > 0) {
					body.append('&');
				}
				body.append(URLEncoder.encode(fields[i], StandardCharsets.UTF_8)).append('=')
						.append(URLEncoder.encode(fields[i + 1], StandardCharsets.UTF_8));
			}
			HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body.toString()));
			return send(builder, headers);
		}

		private HttpResponse<String> send(HttpRequest.Builder builder, String... headers) throws Exception {
			if (headers.length > 0) {
				builder.headers(headers);
			}
			if (!session.isEmpty()) {
				builder.header("Cookie", session);
			}
			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			response.headers().allValues("Set-Cookie").stream()
					.filter(c -> c.startsWith("JSESSIONID="))
					.findFirst()
					.ifPresent(c -> session = c.substring(0, c.indexOf(';')));
			return response;
		}

		Browser login(String email, String password) throws Exception {
			post("/login", "eMail", email, "password", password);
			return this;
		}
	}

	private static Browser admin() throws Exception {
		return new Browser().login("admin@example.com", "admin");
	}

	private static Browser customer() throws Exception {
		return new Browser().login("anna@example.com", "anna");
	}

	private static String location(HttpResponse<?> response) {
		return response.headers().firstValue("Location").orElse("");
	}

	private static void assertRedirect(String expectedLocation, HttpResponse<String> response) {
		assertEquals(302, response.statusCode(), response.body());
		assertEquals(expectedLocation, location(response));
	}

	/** The fields of user-medicines.jsp for the six demo medicines: rowId + quantity per row, then the checked ids. */
	private static String[] cart(String[] quantities, String... checkedIds) {
		List<String> fields = new ArrayList<>();
		for (String id : checkedIds) {
			fields.add("medicineId");
			fields.add(id);
		}
		for (int row = 0; row < quantities.length; row++) {
			fields.add("rowId");
			fields.add(String.valueOf(row + 1));
			fields.add("quantity");
			fields.add(quantities[row]);
		}
		return fields.toArray(new String[0]);
	}

	private static String uniqueEmail() {
		return "web-" + UUID.randomUUID() + "@example.com";
	}

	// ---------- the start page, the logo, the filter for guests ----------

	@Test
	void startPageAndLogoAreOpenToEverybody() throws Exception {
		HttpResponse<String> start = new Browser().get("/");
		assertEquals(200, start.statusCode());
		assertTrue(start.body().contains("<h1>Pharmacy</h1>"), start.body());
		HttpResponse<String> logo = new Browser().get("/images/logo.svg");
		assertEquals(200, logo.statusCode());
		assertTrue(logo.headers().firstValue("Content-Type").orElse("").startsWith("image/svg+xml"));
	}

	@Test
	void guestsAreSentToTheLoginPage() throws Exception {
		Browser guest = new Browser();
		for (String path : List.of("/user-medicines", "/user-cart", "/admin", "/admin-users", "/admin-medicines", "/medicines")) {
			assertRedirect("/login", guest.get(path));
		}
		assertRedirect("/login", guest.post("/admin-medicines", "id", "1"));
		assertTrue(MedicineDao.getInstance().findById(1).isPresent(), "a guest deleted a medicine");
	}

	@Test
	void loginPageHasTheOwnLogoAndTheAuthors() throws Exception {
		String body = new Browser().get("/login").body();
		assertTrue(body.contains("<img alt=\"Pharmacy\" height=\"220\" src=\"/images/logo.svg\">"), body);
		assertFalse(body.contains("twimg.com"), "the old picture of another company");
		assertTrue(body.contains("href=\"https://github.com/DmitryPoverov\""), body);
		assertTrue(body.contains("href=\"/registration\""), "no hard-coded localhost:8080");
		assertTrue(body.contains("Demo accounts:"), body);
		assertTrue(body.contains("value=\"en_US\">English"), "2021: en_EN");
	}

	// ---------- login, roles, logout ----------

	@Test
	void failedLoginKeepsTheEmail() throws Exception {
		Browser browser = new Browser();
		HttpResponse<String> response = browser.post("/login", "eMail", "anna+1@example.com", "password", "wrong");
		// 2021: "/login?error&email=null" - the servlet read "email", the form sends "eMail"
		assertRedirect("/login?error&email=anna%2B1%40example.com", response);
		String page = browser.get(location(response)).body();
		assertTrue(page.contains("value=\"anna+1@example.com\""), page);
		assertTrue(page.contains("Email or password is not correct."), page);
	}

	@Test
	void loginRedirectsByRoleWithANewSessionId() throws Exception {
		Browser browser = new Browser();
		browser.get("/login");
		String before = browser.session;
		assertFalse(before.isEmpty(), "the login page starts a session");
		assertRedirect("/admin", browser.post("/login", "eMail", "admin@example.com", "password", "admin"));
		assertNotEquals(before, browser.session, "session fixation: the id must change after login");
		assertEquals(200, browser.get("/admin").statusCode());

		assertRedirect("/user-medicines", new Browser().post("/login", "eMail", "anna@example.com", "password", "anna"));
	}

	@Test
	void customerCannotUseAdministratorPages() throws Exception {
		Browser anna = customer();
		for (String path : List.of("/admin", "/admin-users", "/admin-medicines", "/admin?view=servlet")) {
			assertRedirect("/user-medicines", anna.get(path));
		}
		// 2021: a USER could delete medicines with this request
		assertRedirect("/user-medicines", anna.post("/admin-medicines", "id", "1", "name", "Aspirin", "country", "Germany", "price", "5"));
		assertTrue(MedicineDao.getInstance().findById(1).isPresent(), "a customer deleted a medicine");
		assertEquals(200, anna.get("/user-medicines").statusCode());
		assertEquals(200, anna.get("/medicines").statusCode());
	}

	@Test
	void logoutEndsTheSession() throws Exception {
		Browser anna = customer();
		assertRedirect("/login", anna.post("/logout"));
		assertRedirect("/login", anna.get("/user-medicines"));
	}

	// ---------- the customer: list, cart, purchase ----------

	@Test
	void cartCountsThePricesOfTheCheckedRows() throws Exception {
		Browser anna = customer();
		String list = anna.get("/user-medicines").body();
		assertTrue(list.contains("<input type=\"hidden\" name=\"rowId\" value=\"2\"/>"), list);
		// 3 x Ibuprofen (7$) + 2 x Validol (4$)
		HttpResponse<String> cart = anna.post("/user-cart", cart(new String[] {"0", "3", "0", "0", "0", "2"}, "2", "6"));
		assertEquals(200, cart.statusCode(), cart.body());
		String body = cart.body().replaceAll("\\s+", " ");
		assertTrue(body.contains("Ibuprofen (USA) - 7$: 3 item(s). <br> Total price of this medicine: 21$"), body);
		assertTrue(body.contains("Validol (Russia) - 4$: 2 item(s). <br> Total price of this medicine: 8$"), body);
		assertTrue(body.contains("Total price: 29$"), body);
		// the cart stays in the session: GET shows it again
		assertTrue(anna.get("/user-cart").body().contains("Total price: 29$"));
	}

	@Test
	void quantityIsTakenFromTheRowOfTheCheckbox() throws Exception {
		Browser anna = customer();
		// Ibuprofen is checked, but the 5 is typed in the row of Validol: 2021 bought 5 x Ibuprofen
		assertRedirect("/user-medicines", anna.post("/user-cart", cart(new String[] {"0", "0", "0", "0", "0", "5"}, "2")));
		String page = anna.get("/user-medicines").body();
		assertTrue(page.contains("Quantity of the medicine(s) entered incorrectly."), page);
		assertFalse(anna.get("/user-medicines").body().contains("entered incorrectly"), "the message is shown once");
	}

	@Test
	void wrongCartFormsAreErrorsNotServerErrors() throws Exception {
		Browser anna = customer();
		List<String[]> forms = List.of(
				cart(new String[] {"0", "0", "0", "0", "0", "0"}),               // nothing checked (2021: NullPointerException)
				cart(new String[] {"0", "-10", "0", "0", "0", "0"}, "2"),        // 2021: total -70$
				cart(new String[] {"0", "abc", "0", "0", "0", "0"}, "2"),        // 2021: NumberFormatException
				cart(new String[] {"0", "", "0", "0", "0", "0"}, "2"),           // 2021: NumberFormatException
				cart(new String[] {"0", "0", "0", "0", "0", "0"}, "2"),          // checked, quantity 0
				cart(new String[] {"0", "1001", "0", "0", "0", "0"}, "2"),       // more than 1000
				cart(new String[] {"0", "3", "0", "0", "0", "5"}, "2"),          // a quantity in a row without a checkbox
				new String[] {"medicineId", "999", "rowId", "999", "quantity", "1"}, // no such medicine (2021: "() - 0$")
				new String[] {"medicineId", "2", "quantity", "1"});              // a form without rowId
		for (String[] form : forms) {
			assertRedirect("/user-medicines", anna.post("/user-cart", form));
			assertTrue(anna.get("/user-medicines").body().contains("entered incorrectly"), String.join(",", form));
		}
	}

	@Test
	void emptyCartGoesBackToTheList() throws Exception {
		// 2021: JspTagException "'end' < 0" (500)
		assertRedirect("/user-medicines", customer().get("/user-cart"));
	}

	@Test
	void submitAndCancelShowTheirMessageOnceAndEmptyTheCart() throws Exception {
		Browser anna = customer();
		anna.post("/user-cart", cart(new String[] {"1", "0", "0", "0", "0", "0"}, "1"));
		assertRedirect("/user-medicines", anna.post("/final-servlet", "submitReject", "2"));
		assertTrue(anna.get("/user-medicines").body().contains("Successful purchase."));
		assertFalse(anna.get("/user-medicines").body().contains("Successful purchase."), "shown once");
		assertRedirect("/user-medicines", anna.get("/user-cart"));

		anna.post("/user-cart", cart(new String[] {"1", "0", "0", "0", "0", "0"}, "1"));
		assertRedirect("/user-medicines", anna.post("/final-servlet", "submitReject", "1"));
		// 2021: never shown - the message was a request attribute, lost with the redirect
		assertTrue(anna.get("/user-medicines").body().contains("Purchase was rejected."));
	}

	@Test
	void finalServletWithoutAButtonOrACartGoesBack() throws Exception {
		Browser anna = customer();
		// 2021: NullPointerException (500)
		assertRedirect("/user-medicines", anna.post("/final-servlet"));
		assertRedirect("/user-medicines", anna.post("/final-servlet", "submitReject", "2"));
		assertFalse(anna.get("/user-medicines").body().contains("Successful purchase."), "no purchase without a cart");
		// with a cart, but without a button
		anna.post("/user-cart", cart(new String[] {"1", "0", "0", "0", "0", "0"}, "1"));
		assertRedirect("/user-medicines", anna.post("/final-servlet"));
	}

	// ---------- the administrator ----------

	@Test
	void administratorPagesInBothViews() throws Exception {
		Browser admin = admin();
		assertTrue(admin.get("/admin").body().contains("List of medicines"));
		String users = admin.get("/admin-users").body();
		assertTrue(users.contains("Id:2, Анна Смирнова - [anna@example.com] - USER"), users);
		String usersByServlet = admin.get("/admin-users?view=servlet").body();
		assertTrue(usersByServlet.startsWith("<h1>Список юзеров:</h1><ul><li> 1 Admin admin@example.com ADMIN</li>"), usersByServlet);
		String medicinesByServlet = admin.get("/admin?view=servlet").body();
		assertTrue(medicinesByServlet.startsWith("<h1>Список лекарств:</h1><ul><li> Aspirin 5 Germany </li>"), medicinesByServlet);
		assertFalse(medicinesByServlet.contains("<html>"), "no JSP involved");
	}

	@Test
	void administratorAddsAndDeletesAMedicine() throws Exception {
		Browser admin = admin();
		String page = admin.post("/admin-medicines", "name", "Корвалол", "countryOfProduction", "Беларусь", "price", "6",
				"medicineAddition", "medicineAddition").body();
		assertTrue(page.contains("Medicine saved."), page);
		assertTrue(page.contains("Корвалол (Беларусь) - 6$"), page);
		Medicine added = MedicineDao.getInstance().findAll().stream()
				.filter(m -> m.getName().equals("Корвалол")).findFirst().orElseThrow();

		// the message belongs to the action, not to every later visit (2021: shown again and again)
		assertFalse(admin.get("/admin-medicines").body().contains("Medicine saved."));

		String deleted = admin.post("/admin-medicines", "id", String.valueOf(added.getId()), "name", "Корвалол",
				"country", "Беларусь", "price", "6").body();
		assertTrue(deleted.contains("Medicine deleted."), deleted);
		String again = admin.post("/admin-medicines", "id", String.valueOf(added.getId())).body();
		assertTrue(again.contains("Medicine was not deleted"), again);
	}

	@Test
	void wrongMedicineFormsAreErrorsNotServerErrors() throws Exception {
		Browser admin = admin();
		int before = MedicineDao.getInstance().findAll().size();
		List<String[]> forms = List.of(
				new String[] {"name", "A", "countryOfProduction", "B", "price", "abc", "medicineAddition", "x"},
				new String[] {"name", "A", "countryOfProduction", "B", "medicineAddition", "x"},
				new String[] {"name", "A", "countryOfProduction", "B", "price", "-1", "medicineAddition", "x"},
				new String[] {"name", " ", "countryOfProduction", "B", "price", "1", "medicineAddition", "x"},
				new String[] {"countryOfProduction", "B", "price", "1", "medicineAddition", "x"});
		for (String[] form : forms) {
			HttpResponse<String> response = admin.post("/admin-medicines", form);
			assertEquals(200, response.statusCode(), String.join(",", form));
			assertTrue(response.body().contains("Medicine was not saved: fill in the name"), response.body());
		}
		// deleting without an id or with a text id (2021: NumberFormatException)
		for (String[] form : List.of(new String[] {"name", "x"}, new String[] {"id", "abc"})) {
			HttpResponse<String> response = admin.post("/admin-medicines", form);
			assertEquals(200, response.statusCode());
			assertTrue(response.body().contains("Medicine was not deleted"), response.body());
		}
		assertEquals(before, MedicineDao.getInstance().findAll().size());
	}

	@Test
	void namesWithTagsAreShownAsText() throws Exception {
		Browser admin = admin();
		admin.post("/admin-medicines", "name", "<b id=xss>X</b>", "countryOfProduction", "C", "price", "1", "medicineAddition", "x");
		String email = uniqueEmail();
		assertRedirect("/login?registered", new Browser().post("/registration", "userName", "<i id=xss>U</i>", "eMail", email, "password", "pw"));
		try {
			for (String body : List.of(admin.get("/admin-medicines").body(), admin.get("/admin-users").body(),
					admin.get("/medicines").body(), admin.get("/admin?view=servlet").body(),
					admin.get("/admin-users?view=servlet").body(), customer().get("/user-medicines").body())) {
				assertFalse(body.contains("<b id=xss>") || body.contains("<i id=xss>"), body);
			}
			assertTrue(admin.get("/admin-medicines").body().contains("&lt;b id=xss&gt;X&lt;/b&gt;"));
			// the pages written by the servlets themselves (HtmlUtil.escape): every character escaped, not only some
			assertTrue(admin.get("/medicines").body().contains("&lt;b id=xss&gt;X&lt;/b&gt;"));
			assertTrue(admin.get("/admin?view=servlet").body().contains("&lt;b id=xss&gt;X&lt;/b&gt;"));
			assertTrue(admin.get("/admin-users?view=servlet").body().contains("&lt;i id=xss&gt;U&lt;/i&gt;"));
		} finally {
			MedicineDao.getInstance().findAll().stream().filter(m -> m.getName().startsWith("<b"))
					.forEach(m -> MedicineDao.getInstance().delete(m.getId()));
			UserDao.getInstance().findAll().stream().filter(u -> u.getEmail().equals(email))
					.forEach(u -> UserDao.getInstance().delete(u.getId()));
		}
	}

	// ---------- registration ----------

	@Test
	void registrationCreatesACustomerWhoCanLogIn() throws Exception {
		String email = uniqueEmail();
		Browser browser = new Browser();
		assertRedirect("/login?registered", browser.post("/registration", "userName", "Иван Петров", "eMail", email, "password", "пароль"));
		assertTrue(browser.get("/login?registered").body().contains("Registration is complete."));
		try {
			User user = UserDao.getInstance().findByEmail(email).orElseThrow();
			assertEquals("Иван Петров", user.getName());
			assertEquals("USER", user.getRole().name());
			assertRedirect("/user-medicines", new Browser().post("/login", "eMail", email, "password", "пароль"));
		} finally {
			UserDao.getInstance().findByEmail(email).ifPresent(u -> UserDao.getInstance().delete(u.getId()));
		}
	}

	@Test
	void registrationErrorsStayOnTheForm() throws Exception {
		// 2021: the empty form - 500 (not-null), the same email twice - 500 (unique), blank fields - saved
		HttpResponse<String> empty = new Browser().post("/registration");
		assertEquals(200, empty.statusCode(), empty.body());
		assertTrue(empty.body().contains("Enter your name."), empty.body());
		assertTrue(empty.body().contains("Enter a valid email"), empty.body());
		assertTrue(empty.body().contains("Enter a password."), empty.body());

		HttpResponse<String> taken = new Browser().post("/registration", "userName", "Anna", "eMail", "anna@example.com", "password", "x");
		assertEquals(200, taken.statusCode());
		assertTrue(taken.body().contains("This email is already registered."), taken.body());
		assertTrue(taken.body().contains("value=\"anna@example.com\""), "the email stays in the field");
		assertEquals(3, UserDao.getInstance().findAll().stream().filter(u -> u.getId() <= 3).count());
	}

	// ---------- the language ----------

	@Test
	void languageIsKeptInTheSessionAndTheAddressIsNotChanged() throws Exception {
		Browser browser = new Browser();
		browser.get("/login");
		// 2021: "...?lang=ru_RU" was appended every time: /login?error&email=a?lang=ru_RU?lang=en_EN
		assertRedirect("/login?error&email=a", browser.postWithHeaders("/locale",
				new String[] {"Referer", baseUrl + "/login?error&email=a&lang=en_US"}, "lang", "ru_RU"));
		String russian = browser.get("/login").body();
		assertTrue(russian.contains("Эл.почта"), russian);
		assertTrue(russian.contains("Демо-аккаунты"), russian);

		// en_EN (the 2021 button) and anything else become en_US; a foreign Referer is cut to its path
		assertRedirect("/registration", browser.postWithHeaders("/locale",
				new String[] {"Referer", "https://example.org/registration"}, "lang", "en_EN"));
		HttpResponse<String> english = browser.get("/login");
		assertTrue(english.body().contains("Email"));
		// fmt:setLocale also sets the language of the response
		assertEquals("en-US", english.headers().firstValue("Content-Language").orElse(""));
		assertRedirect("/registration", browser.post("/locale", "lang", "ru_RU"));
	}

	@Test
	void everyPageHasAllItsTextsInBothLanguages() throws Exception {
		for (String lang : List.of("en_US", "ru_RU")) {
			Browser admin = admin();
			admin.post("/locale", "lang", lang);
			Browser anna = customer();
			anna.post("/locale", "lang", lang);
			anna.post("/user-cart", cart(new String[] {"1", "0", "0", "0", "0", "0"}, "1"));
			List<String> pages = List.of(new Browser().get("/login").body(), new Browser().get("/registration").body(),
					admin.get("/admin").body(), admin.get("/admin-users").body(), admin.get("/admin-medicines").body(),
					admin.post("/admin-medicines", "price", "x", "medicineAddition", "x").body(),
					anna.get("/user-medicines").body(), anna.get("/user-cart").body(),
					new Browser().post("/registration").body());
			for (String page : pages) {
				// fmt:message writes ???key??? for a key that is missing in translations*.properties
				assertFalse(page.contains("???"), lang + ": " + page);
			}
		}
	}
}
