package service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import dao.UserDao;
import dto.UserDto;
import entity.User;
import mapper.UserMapper;

public class UserService {
	
	private static final UserService INSTANCE = new UserService();
	private final UserDao userDao = UserDao.getInstance();
	private final UserMapper userMapper = UserMapper.getInstance();
	
    public Optional<UserDto> login(String email, String password) {
        return userDao.findByEmailAndPassword(email, password)
                .map(userMapper::mapFromUserDtoToUser);
    }
	
	public List<UserDto> findAll() {
		return userDao.findAll().stream().map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole()))
				.collect(Collectors.toList()); 
		
	} 

	public Integer create(UserDto userDto) {
		User userEntity = userMapper.mapFrom(userDto);
		User save = userDao.save(userEntity);
		return save.getId();	
	}
	
	public static UserService getInstance() {
		return INSTANCE;
	}
	
	
}
