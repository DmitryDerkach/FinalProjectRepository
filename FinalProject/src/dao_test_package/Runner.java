package dao_test_package;

import java.util.List;
import java.util.Optional;

import dao.MedicineDao;
import dao.UserDao;
import entity.Medicine;
import entity.Role;
import entity.User;

public class Runner {
	public static void main(String[] args) {
//		MedicineDao test = MedicineDao.getInstance();
//		List<Medicine> listOdMedicine =  test.findAll();
//		for (Medicine i : listOdMedicine) {
//			System.out.println(i);
//		}
//		UserDao test = UserDao.getInstance();
//		List<User> listofUsers = test.findAll();
//		for (User i : listofUsers) {
//			System.out.println(i);
//		}
//		UserDao test = UserDao.getInstance();
//		User user = new User();
//		user.setName("Tom");
//		user.setEmail("123@hemk.com");
//		user.setPassword("123");
//		user.setRole(Role.USER);
//		test.save(user);
		
//		MedicineDao test = MedicineDao.getInstance();
//		test.delete(1);
		MedicineDao test = MedicineDao.getInstance();
		Optional<Medicine> optionalMedicine = test.findById(25);
		Medicine medicine = optionalMedicine.get();
		System.out.println(medicine);
		
	}
}
