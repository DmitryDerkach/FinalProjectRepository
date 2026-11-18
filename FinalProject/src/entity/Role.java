package entity;

/* Описание (2026): роль пользователя. ADMIN управляет лекарствами и видит пользователей (/admin...),
 * USER покупает лекарства (/user-medicines, корзина). В таблице users хранится имя константы. */
public enum Role {
	USER,
	ADMIN
}
