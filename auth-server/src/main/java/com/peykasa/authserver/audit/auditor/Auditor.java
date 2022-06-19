package com.peykasa.authserver.audit.auditor;

/**
 * @author Yaser(amin) Sadeghi
 */

public interface Auditor {
    <S,D> void audit(Audit<S,D> audit);
}
