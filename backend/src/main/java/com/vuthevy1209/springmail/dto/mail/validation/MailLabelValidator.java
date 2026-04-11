package com.vuthevy1209.springmail.dto.mail.validation;

import com.vuthevy1209.springmail.enums.MailLabel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MailLabelValidator implements ConstraintValidator<ValidMailLabel, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // return to return permissions to Blank or Empty
        }
        
        return MailLabel.fromId(value) != null;
    }
}