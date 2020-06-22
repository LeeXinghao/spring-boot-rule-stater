package org.holicc.drools.config;

import org.holicc.drools.FactsProxy;
import org.holicc.drools.FactsService;
import org.holicc.drools.KieSchedule;
import org.holicc.drools.KieTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DroolsProperties.class)
public class DroolsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "kieTemplate")
    public KieTemplate kieTemplate(DroolsProperties droolsProperties) {
        KieTemplate kieTemplate = new KieTemplate();
        kieTemplate.setPath(droolsProperties.getPath());
        kieTemplate.setMode(droolsProperties.getMode());
        kieTemplate.setUpdate(droolsProperties.getUpdate());
        return kieTemplate;
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
        return key -> null;
    }

    @Bean
    public FactsProxy factsProxy(FactsService factsService) {
        return new FactsProxy(factsService);
    }
}
