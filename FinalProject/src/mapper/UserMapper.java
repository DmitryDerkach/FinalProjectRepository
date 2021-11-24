package mapper;

import dto.UserDto;
import entity.User;

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
    
    public UserDto mapFromUserDtoToUser(User object) {
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
