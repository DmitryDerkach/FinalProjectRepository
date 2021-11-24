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

public class UserDao implements Dao<Integer, User> {

	private static final UserDao INSTANCE = new UserDao();
	private static final String FIND_ALL = "select * from users";
	private static final String SAVE = "insert into users ("
			+ "name, email, password, role) values (?, ?, ?, ?)";
    private static final String FIND_BY_EMAIL_AND_PASSWORD_SQL =
            "SELECT * FROM users WHERE email = ? AND password = ?";

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

	@Override
	public Optional<User> findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delete(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(User entity) {
		// TODO Auto-generated method stub
		
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
