package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.auditor.Audit;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@Scope("prototype")
public class EmptyExceptionProvider extends ExceptionProvider {


    public EmptyExceptionProvider() {
        super(null, null);
    }


    @Override
    public Audit<?, AbstractMap.SimpleEntry<String, String>> provide() {
        return null;
    }
}
