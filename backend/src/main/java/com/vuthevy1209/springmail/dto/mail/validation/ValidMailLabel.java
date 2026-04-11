package com.vuthevy1209.springmail.dto.mail.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MailLabelValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE_USE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMailLabel {
    
    String message() default "Label ID is not valid";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}