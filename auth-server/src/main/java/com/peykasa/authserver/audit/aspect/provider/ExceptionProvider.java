package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.DuplicateResourceException;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import com.peykasa.authserver.exception.UnprocessableException;
import com.peykasa.authserver.exception.ValidationException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaser(amin) Sadeghi
 */
public abstract class ExceptionProvider {

    protected final Object[] params;
    protected Throwable t;
    protected List<Class<?>> classes = new ArrayList<>();

    public ExceptionProvider(Object[] params, Throwable t) {
        this.params = params;
        this.t = t;
        classes.add(ResourceNotFoundException.class);
        classes.add(DuplicateResourceException.class);
        classes.add(UnprocessableException.class);
        classes.add(ValidationException.class);
    }

    public abstract Audit<?, AbstractMap.SimpleEntry<String, String>> provide();
}
