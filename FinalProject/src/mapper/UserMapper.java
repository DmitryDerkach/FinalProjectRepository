package mapper;

import dto.UserDto;
import entity.User;

/* Описание (2026): переводит пользователя между слоями: UserDto (форма, сессия) <-> User (DAO, база). */
public class UserMapper {
	private static final UserMapper INSTANCE = new UserMapper();

    public User mapFrom(UserDto object) {
        return User.builder()
                .id(object.getId())
                .name(object.getName())
                .email(object.getEmail())
                .password(object.getPassword())
                .role(object.getRole())
                .build();
    }

    // fix 2026: метод назывался mapFromUserDtoToUser, хотя переводит наоборот - User в UserDto
    public UserDto mapFromUserToUserDto(User object) {
        return UserDto.builder()
                .id(object.getId())
                .name(object.getName())
                .email(object.getEmail())
                .password(object.getPassword())
                .role(object.getRole())
                .build();
    }

	public static UserMapper getInstance() {
		return INSTANCE;
	}

}
