package dao_test_package;

import java.util.List;
import java.util.Optional;

import dao.MedicineDao;
import dao.UserDao;
import entity.Medicine;
import entity.Role;
import entity.User;

/* Описание (2026): консольная проверка DAO без сервера - так в 2021 проверялись запросы к базе до того, как
 * появились сервлеты: каждый раз раскомментировался один кусок. 2026: куски стали шагами (методами),
 * шаг выбирается аргументом, без аргумента выполняются все по порядку:
 *   ./run.sh runner                  все шаги
 *   ./run.sh runner findMedicine     один шаг: medicines, users, saveUser, deleteMedicine, findMedicine */
public class Runner {
	public static void main(String[] args) {
		String step = args.length > 0 ? args[0] : "all";
		boolean all = step.equals("all");
		boolean known = false;
		if (all || step.equals("medicines")) { medicines(); known = true; }
		if (all || step.equals("users")) { users(); known = true; }
		if (all || step.equals("saveUser")) { saveUser(); known = true; }
		if (all || step.equals("deleteMedicine")) { deleteMedicine(); known = true; }
		if (all || step.equals("findMedicine")) { findMedicine(); known = true; }
		if (!known) {
			System.out.println("Unknown step: " + step + " (medicines, users, saveUser, deleteMedicine, findMedicine)");
			System.exit(1);
		}
	}

	// 2026: шаг 1 - все лекарства (MedicineDao.findAll)
	private static void medicines() {
		System.out.println("== medicines");
		MedicineDao test = MedicineDao.getInstance();
		// fix 2026: опечатка listOdMedicine -> listOfMedicine
		List<Medicine> listOfMedicine =  test.findAll();
		for (Medicine i : listOfMedicine) {
			System.out.println(i);
		}
	}

	// 2026: шаг 2 - все пользователи (UserDao.findAll)
	private static void users() {
		System.out.println("== users");
		UserDao test = UserDao.getInstance();
		List<User> listofUsers = test.findAll();
		for (User i : listofUsers) {
			System.out.println(i);
		}
	}

	// 2026: шаг 3 - новый пользователь (UserDao.save возвращает его с id из базы)
	private static void saveUser() {
		System.out.println("== saveUser");
		UserDao test = UserDao.getInstance();
		User user = new User();
		user.setName("Tom");
		// fix 2026: был 123@hemk.com - у вымышленного пользователя адрес на зарезервированном домене example.com
		user.setEmail("tom@example.com");
		user.setPassword("123");
		user.setRole(Role.USER);
		test.save(user);
		System.out.println(user);
	}

	// 2026: шаг 4 - удаление лекарства с id 1 (true - строка была и удалена)
	private static void deleteMedicine() {
		System.out.println("== deleteMedicine");
		MedicineDao test = MedicineDao.getInstance();
		System.out.println("deleted medicine 1: " + test.delete(1));
	}

	// шаг 5 - активный код 2021 года
	private static void findMedicine() {
		System.out.println("== findMedicine");
		MedicineDao test = MedicineDao.getInstance();
		Optional<Medicine> optionalMedicine = test.findById(25);
		// fix 2026: было optionalMedicine.get() - лекарства с id 25 в демо-данных нет; после исправления
		// MedicineDao.findById (пустой Optional вместо "пустого" лекарства) get() бросал бы исключение
		System.out.println(optionalMedicine.map(Medicine::toString).orElse("medicine 25: not found (Optional.empty)"));
		Medicine medicine = test.findById(2).get();
		System.out.println(medicine);

	}
}
