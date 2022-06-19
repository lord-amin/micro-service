package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.exception.ResourceNotFoundException;
import lombok.var;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class GetExceptionProvider extends ExceptionProvider {

    public GetExceptionProvider(Object[] params, Throwable t) {
        super(params, t);

    }

    @Override
    public Audit<AbstractMap.SimpleEntry<String, Object>, AbstractMap.SimpleEntry<String, String>> provide() {
        if (classes.contains(t.getClass())) {
            var messages = ((ResourceNotFoundException) t).getMessages();
            return new Audit<>(new AbstractMap.SimpleEntry<>("id", params[0]), new AbstractMap.SimpleEntry<>("msg", messages.get(0)));
        }
        return null;
    }
}
