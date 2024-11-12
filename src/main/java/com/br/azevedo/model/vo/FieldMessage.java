package com.br.azevedo.model.vo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.validation.FieldError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FieldMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private String fieldName;
	private String message;

	public FieldMessage() {
		super();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static List<FieldMessage> error(List<FieldError> fieldErrors) {
		List<FieldMessage> result = new ArrayList<>();
		fieldErrors.forEach(e -> {
			FieldMessage fm = new FieldMessage();
			String field = e.getField();
			if (field.contains(".")) {
				String[] split = field.split("\\.");
				field = StringUtils.isNotBlank(split[1]) ? split[1] :  field;
			}
			fm.setFieldName(field);
			fm.setMessage(e.getDefaultMessage());
			result.add(fm);
		});		
		return result;
	}
}
