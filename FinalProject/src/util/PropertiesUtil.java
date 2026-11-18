package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/* Описание (2026): читает application.properties из classpath (WEB-INF/classes) один раз при загрузке класса. */
public final class PropertiesUtil {
	private static final Properties PROPERTIES = new Properties();

	static {
		loadProperties();
	}

	private PropertiesUtil() {

	}

	private static void loadProperties() {
		try(InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream
		   ("application.properties")) {
			PROPERTIES.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 2026: значение из командной строки (-Ddb.url=...) важнее файла: так Tomcat в Docker получает адрес
	// базы db:5432, а тесты и Runner - порт, заданный DB_PORT, без правки application.properties
	public static String get(String key) {
		return System.getProperty(key, PROPERTIES.getProperty(key));
	}
}
