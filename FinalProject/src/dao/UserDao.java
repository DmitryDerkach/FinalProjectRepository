package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dto.UserDto;
import entity.Role;
import entity.User;
import lombok.SneakyThrows;
import util.ConnectionManager;

/* Описание (2026): DAO таблицы users - чтение, регистрация (save) и вход (findByEmailAndPassword:
 * пользователь найден только при совпадении и email, и пароля). Синглтон: getInstance(). */
public class UserDao implements Dao<Integer, User> {

	private static final UserDao INSTANCE = new UserDao();
	// fix 2026: + order by id - стабильный порядок в списке пользователей у администратора
	private static final String FIND_ALL = "select * from users order by id";
	private static final String SAVE = "insert into users ("
			+ "name, email, password, role) values (?, ?, ?, ?)";
    private static final String FIND_BY_EMAIL_AND_PASSWORD_SQL =
            "SELECT * FROM users WHERE email = ? AND password = ?";
	// 2026: запросы для методов, которые были заглушками, и для проверки "email уже занят" при регистрации
	private static final String FIND_BY_ID = "select * from users where id = ?";
	private static final String FIND_BY_EMAIL = "select * from users where email = ?";
	private static final String DELETE_BY_ID = "delete from users where id = ?";
	private static final String UPDATE = "update users set name = ?, email = ?, password = ?, role = ? where id = ?";

	@Override
	public List<User> findAll() {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
			ResultSet resultSet = preparedStatement.executeQuery();
			List<User> listOfUsers = new ArrayList();
			while (resultSet.next()) {
				listOfUsers.add(buildUser(resultSet));
			}
			return listOfUsers;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private User buildUser(ResultSet resultSet) throws SQLException {
		return new User(
				resultSet.getObject("id", Integer.class),
				resultSet.getObject("name", String.class),
				resultSet.getObject("email", String.class),
				resultSet.getObject("password", String.class),
				Role.valueOf(resultSet.getObject("role", String.class))
				);
	}

	// fix 2026: был заглушкой и возвращал null вместо Optional (любой вызов падал с NullPointerException)
	@Override
	@SneakyThrows
	public Optional<User> findById(Integer id) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
			preparedStatement.setObject(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			return resultSet.next() ? Optional.of(buildUser(resultSet)) : Optional.empty();
		}
	}

	// fix 2026: был заглушкой (всегда false) - теперь удаляет строку и сообщает, была ли она
	@Override
	@SneakyThrows
	public boolean delete(Integer id) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BY_ID)) {
			preparedStatement.setObject(1, id);
			return preparedStatement.executeUpdate() != 0;
		}
	}

	// fix 2026: был пустой заглушкой - теперь меняет строку по id
	@Override
	@SneakyThrows
	public void update(User entity) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
			preparedStatement.setObject(1, entity.getName());
			preparedStatement.setObject(2, entity.getEmail());
			preparedStatement.setObject(3, entity.getPassword());
			preparedStatement.setObject(4, entity.getRole().toString());
			preparedStatement.setObject(5, entity.getId());
			preparedStatement.executeUpdate();
		}
	}

	@Override
	@SneakyThrows
	public User save(User entity) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setObject(1, entity.getName());
			preparedStatement.setObject(2, entity.getEmail());
			preparedStatement.setObject(3, entity.getPassword());
			preparedStatement.setObject(4, entity.getRole().toString());
			preparedStatement.executeUpdate();
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
			generatedKeys.next();
			entity.setId(generatedKeys.getObject("id", Integer.class));
			return entity;
		}
	}

	public static UserDao getInstance() {
		return INSTANCE;
	}

	@SneakyThrows
	public Optional<User> findByEmailAndPassword(String email, String password) {
        try (var connection = ConnectionManager.get();
                var preparedStatement = connection.prepareStatement(FIND_BY_EMAIL_AND_PASSWORD_SQL)) {
               preparedStatement.setObject(1, email);
               preparedStatement.setObject(2, password);
               var resultSet = preparedStatement.executeQuery();
               User user = null;
               if (resultSet.next()) {
                   user = buildEntity(resultSet);  // ниже приватный метод
               }
               return Optional.ofNullable(user);
           }
	}

	// 2026: для регистрации - занят ли email (раньше повторный email падал ошибкой 500 из базы)
	@SneakyThrows
	public Optional<User> findByEmail(String email) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_EMAIL)) {
			preparedStatement.setObject(1, email);
			ResultSet resultSet = preparedStatement.executeQuery();
			return resultSet.next() ? Optional.of(buildEntity(resultSet)) : Optional.empty();
		}
	}

	private User buildEntity(ResultSet resultSet) throws SQLException {
		return User.builder()
				.id(resultSet.getObject("id", Integer.class))
				.name(resultSet.getObject("name", String.class))
				.email(resultSet.getObject("email", String.class))
				.password(resultSet.getObject("password", String.class))
				.role(Role.valueOf(resultSet.getObject("role", String.class)))
				.build();
	}

}
