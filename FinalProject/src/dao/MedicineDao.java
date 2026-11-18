package dao;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import dto.MedicineDto;
import entity.Medicine;
import lombok.SneakyThrows;
import util.ConnectionManager;

/* Описание (2026): DAO таблицы medicines - единственное место, где лекарства читаются и пишутся SQL-запросами
 * (PreparedStatement с параметрами "?", поэтому SQL-инъекция невозможна). Синглтон: getInstance().
 * Каждый метод берёт новое соединение у ConnectionManager и закрывает его в try-with-resources. */
public class MedicineDao implements Dao<Integer, Medicine> {

	private final static MedicineDao INSTANCE = new MedicineDao();
	// fix 2026: таблица называлась medecines (опечатка) - теперь medicines (db/01_schema.sql)
	// fix 2026: + order by id - без него PostgreSQL может вернуть строки в любом порядке
	// (после удаления и вставки список на странице "прыгал")
	private static final String FIND_ALL = "select * from medicines order by id";
	private static final String DELETE_BY_ID = "delete from medicines where id = ?";
	private static final String SAVE = "insert into medicines (name, price, country_of_production) "
			+ "values (?,?,?)";
	private static final String FIND_BY_ID = "select * from medicines where id = ?";
	// 2026: запрос для метода update, который был заглушкой
	private static final String UPDATE = "update medicines set name = ?, price = ?, country_of_production = ? "
			+ "where id = ?";

	@Override
	@SneakyThrows
	public Optional<Medicine> findById(Integer id) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
				preparedStatement.setInt(1, id);
				ResultSet resultSet = preparedStatement.executeQuery();
				// fix 2026: было new Medicine() - для несуществующего id метод возвращал не пустой Optional,
				// а "пустое" лекарство (id=0, name=null), и в корзине появлялась строка "() - 0$"
				Medicine medicine = null;
				if (resultSet.next()) {
					medicine = new Medicine(
					resultSet.getInt("id"),
					resultSet.getString("name"),
					resultSet.getInt("price"),
					resultSet.getString("country_of_production")
							);

				}
				return Optional.ofNullable(medicine);
			}
	}

	@Override
	public List<Medicine> findAll() {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
			ResultSet resultSet = preparedStatement.executeQuery();
			// fix 2026: опечатки listOfMedecine, buildMedecine -> listOfMedicines, buildMedicine
			List<Medicine> listOfMedicines = new ArrayList();
			while (resultSet.next()) {
				listOfMedicines.add(buildMedicine(resultSet));
			}
			return listOfMedicines;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}//method

	private Medicine buildMedicine(ResultSet resultSet) throws SQLException {
		return new Medicine(
				resultSet.getObject("id", Integer.class),
				resultSet.getObject("name", String.class),
				resultSet.getObject("price", Integer.class),
				resultSet.getObject("country_of_production", String.class)
				);
	}

	@Override
    @SneakyThrows
    public boolean delete(Integer id) {
        try (Connection connection = ConnectionManager.get()) {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BY_ID);
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate()!=0;
        }
    }

	// fix 2026: был пустой заглушкой (TODO Auto-generated method stub) - теперь меняет строку по id
	@Override
	@SneakyThrows
	public void update(Medicine entity) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
			preparedStatement.setObject(1, entity.getName());
			preparedStatement.setObject(2, entity.getPrice());
			preparedStatement.setObject(3, entity.getCountryOfProduction());
			preparedStatement.setObject(4, entity.getId());
			preparedStatement.executeUpdate();
		}
	}

	@Override
    @SneakyThrows
	public Medicine save(Medicine entity) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setObject(1, entity.getName());
			preparedStatement.setObject(2, entity.getPrice());
			preparedStatement.setObject(3, entity.getCountryOfProduction());
			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();
			entity.setId(resultSet.getObject("id", Integer.class));
			return entity;
		}

	}

	public static MedicineDao getInstance() {
		return INSTANCE;
	}

}
