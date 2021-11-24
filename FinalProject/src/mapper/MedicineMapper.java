package mapper;

import dto.MedicineDto;
import entity.Medicine;

public class MedicineMapper {
	private static final MedicineMapper INSTANCE = new MedicineMapper();

	public Medicine mapFromMedicineDtoToMedicine(MedicineDto object) {
		Medicine medicine = new Medicine();
		try {
			Medicine.builder().id(object.getId()).build();
		} catch (Exception e) {
		  medicine = Medicine.builder()
					.name(object.getName())
					.price(object.getPrice())
					.countryOfProduction(object.getCountryOfProduction())
					.build();
		} 
		if (medicine.getName() == null) {
			medicine = Medicine.builder()
					 .id(object.getId())
					 .name(object.getName())
					 .price(object.getPrice())
					 .countryOfProduction(object.getCountryOfProduction())
					 .build();
		} 
		return medicine;
	}
	
	public static MedicineMapper getInstance() {
		return INSTANCE;
	}

	public MedicineDto mapFromMedicineToMedicineDto(Medicine medicine) {
		return MedicineDto.builder()
				.id(medicine.getId())
				.name(medicine.getName())
				.price(medicine.getPrice())
				.countryOfProduction(medicine.getCountryOfProduction())
				.build();
	}
	
}
