package com.br.azevedo.validation.model;

import com.br.azevedo.utils.MoedaUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import java.math.BigDecimal;

public class NotNullAndPositiveOrZeroCustomValidator implements ConstraintValidator<CustomNotNullAndPositiveOrZero, String> {

    private String fieldName;

    @Override
    public void initialize(CustomNotNullAndPositiveOrZero constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        // Define o nome do campo: usa `fieldName` se definido; caso contrário, o nome original do campo
        String campo = fieldName.isEmpty() ? ((ConstraintValidatorContextImpl) context).getConstraintViolationCreationContexts().get(0).getPath().asString() : fieldName;
        try {
            if (StringUtils.isBlank(valor)) {
                throw new IllegalArgumentException(" O campo '" + campo +"' é obrigatório");
            }

            MoedaUtils.verificarFormatoBrasileiro(valor);
            BigDecimal v = MoedaUtils.stringToBigDecimal(valor);
            return v.compareTo(BigDecimal.ZERO) >= 0;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getLocalizedMessage())
                    .addPropertyNode(campo) // Adiciona o nome do campo
                    .inContainer(Class.class, 0)
                    .addConstraintViolation();
            return false; // Se o valor não for um número válido
        }
    }
}
