package com.enzelascripts.securediv.validation;

import com.enzelascripts.securediv.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private long maxSize;
    private String[] allowedTypes;


    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.maxSize =  constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();

    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {

        if (multipartFile == null || multipartFile.isEmpty()) return false;
        if(multipartFile.getSize() > maxSize) return false;
        return Arrays.asList(allowedTypes).contains(multipartFile.getContentType());
    }
}
