package dto;

import javax.servlet.http.Part;

import entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Builder
public class UserDto {
	 Integer id;
	 String name;
	 String email;
	 String password;
	 Role role;
}
