package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import dao.UserDao;
import dto.UserDto;
import entity.User;
import exception.ValidationException;
import mapper.UserMapper;

/* Описание (2026): бизнес-логика пользователей: вход (login), список для администратора, регистрация (create). */
public class UserService {

	private static final UserService INSTANCE = new UserService();
	// 2026: длина колонок name, email, password в db/01_schema.sql
	private static final int MAX_LENGTH = 128;
	private final UserDao userDao = UserDao.getInstance();
	private final UserMapper userMapper = UserMapper.getInstance();

    public Optional<UserDto> login(String email, String password) {
        return userDao.findByEmailAndPassword(email, password)
                .map(userMapper::mapFromUserToUserDto);
    }

	public List<UserDto> findAll() {
		return userDao.findAll().stream().map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole()))
				.collect(Collectors.toList());

	}

	public Integer create(UserDto userDto) {
		// fix 2026: проверка формы. Раньше пустые поля сохранялись, а отсутствующие поля и повторный email
		// давали ошибку 500 из базы (not-null / unique)
		validate(userDto);
		User userEntity = userMapper.mapFrom(userDto);
		User save = userDao.save(userEntity);
		return save.getId();
	}

	// 2026: ошибки - ключи переводов (translations*.properties), их показывает registration.jsp
	private void validate(UserDto userDto) {
		List<String> errors = new ArrayList<>();
		if (isBlank(userDto.getName())) {
			errors.add("error.name.empty");
		}
		if (isBlank(userDto.getEmail()) || !userDto.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
			errors.add("error.email.invalid");
		}
		if (isBlank(userDto.getPassword())) {
			errors.add("error.password.empty");
		}
		if (tooLong(userDto.getName()) || tooLong(userDto.getEmail()) || tooLong(userDto.getPassword())) {
			errors.add("error.too.long");
		}
		if (errors.isEmpty() && userDao.findByEmail(userDto.getEmail()).isPresent()) {
			errors.add("error.email.taken");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static boolean tooLong(String value) {
		return value != null && value.length() > MAX_LENGTH;
	}

	public static UserService getInstance() {
		return INSTANCE;
	}


}
