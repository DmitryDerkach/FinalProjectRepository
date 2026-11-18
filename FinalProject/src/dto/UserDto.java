package dto;

import javax.servlet.http.Part;

import entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/* Описание (2026): DTO пользователя. Форма регистрации собирается в UserDto, а после входа UserDto лежит
 * в сессии под именем "user" - по нему AuthorizationFilter узнаёт, кто вошёл и с какой ролью. */
// fix 2026: implements Serializable - UserDto лежит в сессии; Tomcat сохраняет сессии на диск при остановке
// (без этого вошедший пользователь терялся при перезапуске сервера, а в лог шёл NotSerializableException)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Builder
public class UserDto implements java.io.Serializable {
	 Integer id;
	 String name;
	 String email;
	 String password;
	 Role role;
}
