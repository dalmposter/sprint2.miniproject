package eMarket.controller;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


public class ItemValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ItemFormDto.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ItemFormDto dto = (ItemFormDto) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productId", "", "Field cannot be empty.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "", "Field cannot be empty.");
		
		if(dto.getAmount() <= 0)
		{
			errors.rejectValue("amount", "", "Amount must be a positive integer");
		}
		
		if(dto.getProductId() < 0)
		{
			errors.rejectValue("amount", "", "Must select a product");
		}
	}
		
}