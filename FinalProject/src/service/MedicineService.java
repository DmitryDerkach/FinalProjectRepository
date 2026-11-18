package service;

import dao.MedicineDao;
import dto.*;
import entity.Medicine;
import mapper.MedicineMapper;

import java.util.*;
import java.util.stream.Collectors;

/* Описание (2026): бизнес-логика лекарств между сервлетами и MedicineDao: отдаёт DTO, сохраняет и удаляет,
 * возвращая строку-результат, которую admin-medicines.jsp показывает администратору. */
public class MedicineService {

	private static final MedicineService INSTANCE = new MedicineService();
	private final MedicineDao medicineDao = MedicineDao.getInstance();
	private final MedicineMapper medicineMapper = MedicineMapper.getInstance();

	private MedicineService() {

	}

	public List<MedicineDto> findAll(){
		return medicineDao.findAll().stream().map(medicines -> new MedicineDto(medicines.getId(), medicines.getName(), medicines.getPrice(), medicines.getCountryOfProduction()))
				.collect(Collectors.toList());
	}

	public static MedicineService getInstance() {
		return INSTANCE;
	}

	public String delete (MedicineDto medicineDto) {
        Medicine medicine = medicineMapper.mapFromMedicineDtoToMedicine(medicineDto);
        boolean delete = medicineDao.delete(medicine.getId());
        if (delete) {
            // fix 2026: опечатка Succsessfuly -> Successfully (строку сравнивает admin-medicines.jsp)
            return "Successfully deleted";
        } else {
            return "Was not deleted";
        }
    }

	public String save(MedicineDto medicineDto) {
		// fix 2026: опечатка responce -> response
		String response = null;
		Medicine medicine = medicineMapper.mapFromMedicineDtoToMedicine(medicineDto);
		Medicine result = medicineDao.save(medicine);
		if (result != null) {
			response = "Saved";
		} else {
			response = "Not saved";
		}
		return response;

	}

	// fix 2026: findbyId -> findById, и результат - Optional: для несуществующего id было optionalMedicine.get()
	// (после исправления MedicineDao.findById это NoSuchElementException и ошибка 500 в корзине)
	public Optional<MedicineDto> findById(int value) {
		Optional<Medicine> optionalMedicine = medicineDao.findById(value);
		return optionalMedicine.map(medicineMapper::mapFromMedicineToMedicineDto);
	}

}
