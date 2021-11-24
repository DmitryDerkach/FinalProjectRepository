package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Builder
public class MedicineDto {
	int id;
	String name;
	int price;
	String countryOfProduction;
}
