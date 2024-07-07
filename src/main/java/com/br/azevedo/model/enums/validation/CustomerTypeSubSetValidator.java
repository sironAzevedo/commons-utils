package com.br.azevedo.model.enums.validation;


import com.br.azevedo.model.enums.StatusEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CustomerTypeSubSetValidator implements ConstraintValidator<CustomerTypeStatusSubset, StatusEnum> {
    private StatusEnum[] subset;

    @Override
    public void initialize(CustomerTypeStatusSubset constraint) {
        this.subset = constraint.anyOf();
    }

    @Override
    public boolean isValid(StatusEnum value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            String descricao = (String) value.getClass().getMethod("getDescricao").invoke(value);
            return Arrays.asList(subset).contains(StatusEnum.from(descricao));
        } catch (Exception e) {
            return false;
        }
    }
}
