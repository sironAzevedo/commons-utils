package com.br.azevedo.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstraticEntity {

    protected abstract Set<ConstraintViolation<AbstraticEntity>> customValidation(Validator validator);

    protected void validate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AbstraticEntity>> violations = new HashSet<>(validator.validate(this));
        Set<ConstraintViolation<AbstraticEntity>> constraintViolations = this.customValidation(validator);
        if(constraintViolations != null) {
            violations.addAll(constraintViolations);
        }

        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
