package datawave.microservice.audit.auditors.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datawave.webservice.common.audit.AuditParameters;
import datawave.webservice.common.audit.Auditor;

/**
 * An implementation for {@link Auditor}, which writes audit messages to the log.
 */
public class LogAuditor implements Auditor {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void audit(AuditParameters am) throws Exception {
        log.info(am.toString());
    }
}
