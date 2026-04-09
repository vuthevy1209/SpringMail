package com.vuthevy1209.springmail.service.gmail.dto.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleErrorDto {
	ErrorDetail error;

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ErrorDetail {
		int code;
		String message;
		String status;
		List<InnerError> errors;
	}

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class InnerError {
		String message;
		String domain;
		String reason;
		String location;
		String locationType;
	}
}
