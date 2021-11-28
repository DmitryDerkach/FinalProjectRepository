package service;

import dao.MedicineDao;
import dto.*;
import entity.Medicine;
import mapper.MedicineMapper;

import java.util.*;
import java.util.stream.Collectors;
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
            return "Succsessfuly deleted";
        } else {
            return "Was not deleted";
        }
    }

	public String save(MedicineDto medicineDto) {
		String responce = null;
		Medicine medicine = medicineMapper.mapFromMedicineDtoToMedicine(medicineDto);
		Medicine result = medicineDao.save(medicine);
		if (result != null) {
			responce = "Saved";
		} else {
			responce = "Not saved";
		}
		return responce;
		
	}

	public MedicineDto findbyId(int value) {
		Optional<Medicine> optionalMedicine = medicineDao.findById(value);
		Medicine medicine = optionalMedicine.get();
		MedicineDto medicineDto = medicineMapper.mapFromMedicineToMedicineDto(medicine);
		return medicineDto;
	}
	
}
