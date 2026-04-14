package com.enzelascripts.securediv.annotation;

import com.enzelascripts.securediv.validation.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "Invalid file";
    String[] allowedTypes() default {"image/png","image/jpeg", "image/jpg"};
    long maxSize() default 512_000;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
