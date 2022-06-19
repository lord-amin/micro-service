package com.peykasa.authserver.audit.aspect;

import com.peykasa.authserver.audit.aspect.provider.AuditProvider;
import com.peykasa.authserver.audit.aspect.provider.EmptyExceptionProvider;
import com.peykasa.authserver.audit.aspect.provider.ExceptionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    Class<? extends AuditProvider> provider();

    Class<? extends ExceptionProvider> exProvider() default EmptyExceptionProvider.class;

    String context();

    String event();

}