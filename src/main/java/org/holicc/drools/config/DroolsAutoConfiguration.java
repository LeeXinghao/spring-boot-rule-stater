package org.holicc.drools.config;

import org.holicc.drools.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(DroolsProperties.class)
public class DroolsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "kieTemplate")
    public KieTemplate kieTemplate(DroolsProperties droolsProperties) {
        return new KieTemplate(droolsProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "kieSchedule")
    public KieSchedule kieSchedule(KieTemplate kieTemplate) {
        KieSchedule kieSchedule = new KieSchedule(kieTemplate);
        kieSchedule.execute();
        return kieSchedule;
    }

    @Bean
    @ConditionalOnMissingBean(value = {FactsService.class})
    public FactsService factsService() {
        return new FactsService() {
            @Override
            public Object get(String key) {
                return null;
            }

            @Override
            public Map<String,Object> doService(String serviceName, Object params) {
                return null;
            }
        };
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FactsProxy factsProxy(FactsService factsService) {
        return new FactsProxy(factsService);
    }
}
