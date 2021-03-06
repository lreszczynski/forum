package com.example.demo.users.validation;

import com.example.demo.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
	UserService userService;
	
	@Autowired
	public PasswordValidator(UserService userService) {
		this.userService = userService;
	}
	
	public void initialize(PasswordConstraint constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(String password, ConstraintValidatorContext cxt) {
		cxt.disableDefaultConstraintViolation();
		
		if (password.length() < 8)
		{
			cxt.buildConstraintViolationWithTemplate("Password should contain at least 8 characters")
					.addPropertyNode("password").addConstraintViolation();
			return false;
		}
		String upperCaseChars = "(.*[A-Z].*)";
		if (!password.matches(upperCaseChars ))
		{
			cxt.buildConstraintViolationWithTemplate("Password must have at least one uppercase character")
					.addPropertyNode("password").addConstraintViolation();
			return false;
		}
		String lowerCaseChars = "(.*[a-z].*)";
		if (!password.matches(lowerCaseChars ))
		{
			cxt.buildConstraintViolationWithTemplate("Password must have at least one lowercase character")
					.addPropertyNode("password").addConstraintViolation();
			return false;
		}
		String numbers = "(.*[0-9].*)";
		if (!password.matches(numbers ))
		{
			cxt.buildConstraintViolationWithTemplate("Password must have at least one number")
					.addPropertyNode("password").addConstraintViolation();
			return false;
		}
		String specialChars = "(.*[^A-Za-z0-9].*)";
		if (!password.matches(specialChars ))
		{
			cxt.buildConstraintViolationWithTemplate("Password must have at least one special character like @,#,$,%")
					.addPropertyNode("password").addConstraintViolation();
			return false;
		}
		return true;
	}
	
	
}