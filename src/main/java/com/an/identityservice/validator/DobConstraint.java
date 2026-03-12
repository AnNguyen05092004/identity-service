package com.an.identityservice.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


// Custom annotation để validate ngày sinh
@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = {DobValidator.class }) // Chỉ định class DobValidator sẽ thực hiện logic validate cho annotation này
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    int min();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };


}
