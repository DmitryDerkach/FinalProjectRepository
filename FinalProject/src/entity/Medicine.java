package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Описание (2026): сущность "лекарство" - одна строка таблицы medicines (цена в целых долларах).
 * Геттеры, сеттеры, equals/hashCode/toString, конструкторы и builder() пишет Lombok при компиляции. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {
	private int id;
	private String name;
	private int price;
	private String countryOfProduction;
}
