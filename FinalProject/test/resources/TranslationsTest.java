package resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/* Описание (2026): оба файла переводов содержат одни и те же ключи. Ключ, которого нет в русском файле, не даёт
 * ошибки на странице: ResourceBundle молча берёт текст из translations.properties, и посреди русской страницы
 * появляется английская фраза. Поэтому ключи сверяются здесь, а не по страницам. */
class TranslationsTest {

	private static Properties load(String name) throws Exception {
		Properties properties = new Properties();
		try (InputStream in = TranslationsTest.class.getClassLoader().getResourceAsStream(name)) {
			properties.load(in);
		}
		return properties;
	}

	@Test
	void bothLanguagesHaveTheSameKeys() throws Exception {
		Properties english = load("translations.properties");
		Properties russian = load("translations_ru_RU.properties");
		assertEquals(new TreeSet<>(english.stringPropertyNames()), new TreeSet<>(russian.stringPropertyNames()));
		for (String key : english.stringPropertyNames()) {
			assertFalse(russian.getProperty(key).isBlank(), key);
		}
	}
}
