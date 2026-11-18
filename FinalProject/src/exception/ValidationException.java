package exception;

import java.util.List;

import lombok.Getter;

/* Описание (2026): добавлено в 2026. Ошибки формы регистрации: список ключей из translations*.properties
 * (error.name.empty, error.email.taken, ...). RegistrationServlet показывает их на той же странице на
 * выбранном языке - вместо прежней ошибки 500 из базы или сохранения пустых полей. */
@Getter
public class ValidationException extends RuntimeException {

	private final List<String> errors;

	public ValidationException(List<String> errors) {
		super(String.join(", ", errors));
		this.errors = errors;
	}
}
