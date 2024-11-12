package com.br.azevedo.validation.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NotNullAndPositiveOrZeroCustomValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface CustomNotNullAndPositiveOrZero {
    String message() default "O valor deve ser positivo ou zero.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String fieldName() default "";
}
