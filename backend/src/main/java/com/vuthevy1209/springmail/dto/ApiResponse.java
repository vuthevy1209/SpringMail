package com.vuthevy1209.springmail.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
	@Builder.Default
	String code = "1000";

	String message;

	T result;
}