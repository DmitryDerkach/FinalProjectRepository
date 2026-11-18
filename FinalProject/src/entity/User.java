 package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/* Описание (2026): сущность "пользователь" - одна строка таблицы users (email уникален, роль - Role). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	private Integer id;
	private String name;
	private String email;
	private String password;
	private Role role;
}
