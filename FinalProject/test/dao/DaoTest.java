package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import dto.MedicineDto;
import dto.UserDto;
import entity.Medicine;
import entity.Role;
import entity.User;
import exception.ValidationException;
import service.MedicineService;
import service.UserService;

/* Описание (2026): тесты DAO и сервисов на живой базе pharmacy_database (PostgreSQL в Docker на порту 5435,
 * ./run.sh test сбрасывает её перед запуском). Тест, который что-то создаёт, удаляет созданное, поэтому
 * демо-данные (6 лекарств, 3 пользователя) остаются как были. */
@Timeout(value = 30, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DaoTest {

	private final MedicineDao medicineDao = MedicineDao.getInstance();
	private final UserDao userDao = UserDao.getInstance();
	private final MedicineService medicineService = MedicineService.getInstance();
	private final UserService userService = UserService.getInstance();

	private static String uniqueEmail() {
		return "dao-" + UUID.randomUUID() + "@example.com";
	}

	@Test
	void findAllReadsTheMedicinesInIdOrder() {
		// an update writes a new version of the row at the end of the table: without "order by"
		// PostgreSQL would now return medicine 1 last
		medicineDao.update(medicineDao.findById(1).orElseThrow());
		List<Integer> ids = medicineDao.findAll().stream().map(Medicine::getId).collect(Collectors.toList());
		assertEquals(List.of(1, 2, 3, 4, 5, 6), ids.subList(0, 6));
		assertEquals(new Medicine(4, "Активированный уголь", 2, "Беларусь"), medicineDao.findById(4).orElseThrow());
	}

	@Test
	void findByIdOfAMissingMedicineIsEmpty() {
		// 2021: an "empty" Medicine(id=0, name=null) instead of Optional.empty()
		assertEquals(Optional.empty(), medicineDao.findById(-1));
		assertEquals(Optional.empty(), medicineService.findById(-1));
		assertEquals("Ibuprofen", medicineService.findById(2).orElseThrow().getName());
	}

	@Test
	void medicineSaveUpdateDelete() {
		Medicine saved = medicineDao.save(new Medicine(0, "Test medicine", 11, "Testland"));
		assertTrue(saved.getId() > 6);
		try {
			saved.setPrice(12);
			saved.setCountryOfProduction("Other");
			medicineDao.update(saved);
			assertEquals(new Medicine(saved.getId(), "Test medicine", 12, "Other"), medicineDao.findById(saved.getId()).orElseThrow());
		} finally {
			assertTrue(medicineDao.delete(saved.getId()));
		}
		assertFalse(medicineDao.delete(saved.getId()), "already deleted");
	}

	@Test
	void medicineServiceSavesAndDeletesWithMessages() {
		assertEquals("Saved", medicineService.save(MedicineDto.builder().name("Service test").price(1).countryOfProduction("X").build()));
		MedicineDto saved = medicineService.findAll().stream()
				.filter(m -> m.getName().equals("Service test")).findFirst().orElseThrow();
		assertEquals("Successfully deleted", medicineService.delete(saved));
		assertEquals("Was not deleted", medicineService.delete(saved));
	}

	@Test
	void userDaoFindByIdUpdateDelete() {
		// the three methods were stubs: findById returned null, delete false, update did nothing
		assertEquals("admin@example.com", userDao.findById(1).orElseThrow().getEmail());
		assertEquals(Optional.empty(), userDao.findById(-1));

		User user = userDao.save(new User(null, "Dao Test", uniqueEmail(), "pw", Role.USER));
		assertNotNull(user.getId());
		try {
			user.setName("Dao Test 2");
			user.setRole(Role.ADMIN);
			userDao.update(user);
			User read = userDao.findById(user.getId()).orElseThrow();
			assertEquals("Dao Test 2", read.getName());
			assertEquals(Role.ADMIN, read.getRole());
		} finally {
			assertTrue(userDao.delete(user.getId()));
		}
		assertEquals(Optional.empty(), userDao.findById(user.getId()));
	}

	@Test
	void loginNeedsTheEmailAndThePassword() {
		assertEquals(Role.ADMIN, userService.login("admin@example.com", "admin").orElseThrow().getRole());
		assertEquals("Анна Смирнова", userService.login("anna@example.com", "anna").orElseThrow().getName());
		assertEquals(Optional.empty(), userService.login("anna@example.com", "admin"));
		assertEquals(Optional.empty(), userService.login(null, null));
		// as with the medicines: after an update the row is physically last, "order by id" keeps the list in order
		userDao.update(userDao.findById(1).orElseThrow());
		assertEquals(List.of(1, 2, 3), userService.findAll().stream().map(UserDto::getId).collect(Collectors.toList()).subList(0, 3));
	}

	@Test
	void registrationValidatesTheForm() {
		ValidationException empty = assertThrows(ValidationException.class,
				() -> userService.create(UserDto.builder().role(Role.USER).build()));
		assertEquals(List.of("error.name.empty", "error.email.invalid", "error.password.empty"), empty.getErrors());

		ValidationException blank = assertThrows(ValidationException.class,
				() -> userService.create(UserDto.builder().name("  ").email("not-an-email").password("").role(Role.USER).build()));
		assertEquals(List.of("error.name.empty", "error.email.invalid", "error.password.empty"), blank.getErrors());

		ValidationException tooLong = assertThrows(ValidationException.class,
				() -> userService.create(UserDto.builder().name("x".repeat(129)).email("a@example.com").password("pw").role(Role.USER).build()));
		assertEquals(List.of("error.too.long"), tooLong.getErrors());

		ValidationException taken = assertThrows(ValidationException.class,
				() -> userService.create(UserDto.builder().name("Anna 2").email("anna@example.com").password("pw").role(Role.USER).build()));
		assertEquals(List.of("error.email.taken"), taken.getErrors());

		String email = uniqueEmail();
		Integer id = userService.create(UserDto.builder().name("Новый покупатель").email(email).password("pw").role(Role.USER).build());
		try {
			assertEquals("Новый покупатель", userDao.findById(id).orElseThrow().getName());
		} finally {
			userDao.delete(id);
		}
	}
}
