package com.peykasa.authserver.audit.aspect.provider;

import com.peykasa.authserver.audit.auditor.Audit;

/**
 * @author Yaser(amin) Sadeghi
 */
public abstract class AuditProvider {

    private Object returning;
    Object[] params;

    public AuditProvider(Object returning, Object[] params) {
        this.returning = returning;
        this.params = params;
    }

    public abstract Audit<?, ?> provide();

    public Object getReturning() {
        return returning;
    }

    public void setReturning(Object returning) {
        this.returning = returning;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
