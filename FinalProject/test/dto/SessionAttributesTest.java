package dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import entity.Role;

/* Описание (2026): всё, что приложение кладёт в сессию, должно переживать сериализацию - так Tomcat сохраняет
 * сессии на диск при остановке (SESSIONS.ser) и читает их при запуске. Здесь то же самое делается в памяти:
 * пользователь (атрибут "user") и корзина (списки "medicines", "medicineDtos", "quantity"). */
class SessionAttributesTest {

	@SuppressWarnings("unchecked")
	private static <T> T copy(T object) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(object);
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			return (T) in.readObject();
		}
	}

	@Test
	void theSessionUserSurvivesATomcatRestart() throws Exception {
		UserDto user = UserDto.builder().id(2).name("Анна").email("anna@example.com").password("anna").role(Role.USER).build();
		assertEquals(user, copy(user));
	}

	@Test
	void theCartSurvivesATomcatRestart() throws Exception {
		ArrayList<MedicineDto> cart = new ArrayList<>(List.of(new MedicineDto(2, "Ibuprofen", 7, "USA")));
		assertEquals(cart, copy(cart));
	}
}
