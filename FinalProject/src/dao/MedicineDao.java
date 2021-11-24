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

public class MedicineDao implements Dao<Integer, Medicine> {
	
	private final static MedicineDao INSTANCE = new MedicineDao();
	private static final String FIND_ALL = "select * from medecines";
	private static final String DELETE_BY_ID = "delete from medecines where id = ?";
	private static final String SAVE = "insert into medecines (name, price, country_of_production) "
			+ "values (?,?,?)";
	private static final String FIND_BY_ID = "select * from medecines where id = ?";
	
	@Override
	@SneakyThrows
	public Optional<Medicine> findById(Integer id) {
		try(Connection connection = ConnectionManager.get();
			PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
				preparedStatement.setInt(1, id);
				ResultSet resultSet = preparedStatement.executeQuery();
				Medicine medicine = new Medicine();
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
			List<Medicine> listOfMedecine = new ArrayList();
			while (resultSet.next()) {
				listOfMedecine.add(buildMedecine(resultSet));
			}
			return listOfMedecine;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	
	}//method

	private Medicine buildMedecine(ResultSet resultSet) throws SQLException {
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

	@Override
	public void update(Medicine entity) {
		// TODO Auto-generated method stub
		
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
