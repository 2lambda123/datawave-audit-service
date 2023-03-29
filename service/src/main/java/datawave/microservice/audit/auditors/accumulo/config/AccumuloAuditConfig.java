package datawave.microservice.audit.auditors.accumulo.config;

import javax.annotation.Resource;

import datawave.microservice.audit.auditors.accumulo.health.AccumuloHealthChecker;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.SubscribableChannel;

import datawave.microservice.audit.auditors.accumulo.AccumuloAuditor;
import datawave.microservice.audit.auditors.accumulo.config.AccumuloAuditProperties.Accumulo;
import datawave.microservice.audit.common.AuditMessage;
import datawave.microservice.audit.common.AuditMessageHandler;
import datawave.webservice.common.audit.AuditParameters;
import datawave.webservice.common.audit.Auditor;

/**
 * Configures the AccumuloAuditor to process messages received by the audit service. This configuration is activated via the 'audit.auditors.accumulo.enabled'
 * property. When enabled, this configuration will also enable the appropriate Spring Cloud Stream configuration for the accumulo audit binding, as specified in
 * the audit config.
 */
@Configuration
@EnableConfigurationProperties(AccumuloAuditProperties.class)
@EnableBinding(AccumuloAuditConfig.AccumuloAuditBinding.class)
@ConditionalOnProperty(name = "audit.auditors.accumulo.enabled", havingValue = "true")
public class AccumuloAuditConfig {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Resource(name = "msgHandlerAuditParams")
    private AuditParameters msgHandlerAuditParams;
    
    @Bean
    public AuditMessageHandler accumuloAuditMessageHandler(Auditor accumuloAuditor) {
        return new AuditMessageHandler(msgHandlerAuditParams, accumuloAuditor) {
            @Override
            @StreamListener(AccumuloAuditBinding.NAME)
            public void onMessage(AuditMessage msg) throws Exception {
                super.onMessage(msg);
            }
        };
    }
    
    @Bean
    public AccumuloAuditor accumuloAuditor(AccumuloAuditProperties accumuloAuditProperties, AccumuloClient client) {
        return new AccumuloAuditor(accumuloAuditProperties.getTableName(), client);
    }
    
    @Bean
    @ConditionalOnProperty(name = "audit.auditors.accumulo.health.enabled", havingValue = "true")
    public AccumuloHealthChecker accumuloHealthChecker(AccumuloAuditProperties accumuloAuditProperties, AccumuloAuditor accumuloAuditor) {
        return new AccumuloHealthChecker(accumuloAuditProperties, accumuloAuditor);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public AccumuloClient accumuloClient(AccumuloAuditProperties accumuloAuditProperties) {
        Accumulo accumulo = accumuloAuditProperties.getAccumuloConfig();
        // @formatter:off
        return org.apache.accumulo.core.client.Accumulo.newClient()
                .to(accumulo.getInstanceName(), accumulo.getZookeepers())
                .as(accumulo.getUsername(), new PasswordToken(accumulo.getPassword()))
                .build();
        // @formatter:on
    }
    
    public interface AccumuloAuditBinding {
        String NAME = "accumuloAuditSink";
        
        @Input(NAME)
        SubscribableChannel accumuloAuditSink();
    }
}
