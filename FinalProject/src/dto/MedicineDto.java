package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

/* Описание (2026): DTO (Data Transfer Object) лекарства - то, что сервис отдаёт сервлетам и JSP
 * (${medicine.name} в JSP вызывает getName()). Без сеттеров: объект только читают. */
// fix 2026: implements Serializable - список лекарств и корзина лежат в сессии, а Tomcat при остановке сохраняет
// сессии на диск и при запуске читает обратно. Без Serializable он писал NotSerializableException в лог,
// и после перезапуска сервера все покупатели оказывались разлогинены с пустой корзиной
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Builder
public class MedicineDto implements java.io.Serializable {
	int id;
	String name;
	int price;
	String countryOfProduction;
}
