package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Описание (2026): выдаёт соединения с PostgreSQL по db.url / db.user / db.password из application.properties.
 * Пула нет: каждый вызов get() открывает новое соединение, а DAO закрывает его (try-with-resources).
 * Class.forName загружает драйвер явно - в веб-приложении Tomcat автоматическая регистрация драйвера
 * (ServiceLoader) срабатывает не всегда. */
public final class ConnectionManager {

	private static final String URL_KEY = "db.url";
	private static final String USER_KEY = "db.user";
	private static final String PASSWORD_KEY = "db.password";

	static {
		loadDriver();
	}


	private static void loadDriver() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private ConnectionManager() {

	}

	public static Connection get() {
		try {
			return DriverManager.getConnection(
					PropertiesUtil.get(URL_KEY),
					PropertiesUtil.get(USER_KEY),
					PropertiesUtil.get(PASSWORD_KEY));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
