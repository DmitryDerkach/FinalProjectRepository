package dao;

import java.util.List;
import java.util.Optional;

/* Описание (2026): общий контракт DAO (Data Access Object) - CRUD-операции над одной таблицей.
 * K - тип ключа (id), T - сущность. Реализации: MedicineDao (таблица medicines), UserDao (таблица users). */
public interface Dao <K,T> {
	List<T> findAll();

	Optional<T> findById(K id);

	boolean delete(K id);

	void update (T entity);

	T save(T entity);
}