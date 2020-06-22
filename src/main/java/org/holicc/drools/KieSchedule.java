package org.holicc.drools;

import org.holicc.drools.util.ScheduledThreadPoolExecutorUtil;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.TimeUnit;


public class KieSchedule implements InitializingBean {

    private KieTemplate kieTemplate;

    public KieSchedule(KieTemplate kieTemplate) {
        this.kieTemplate = kieTemplate;
    }

    public void execute() {
        Long update = kieTemplate.getUpdate();
        if (update == null || update == 0L) {
            update = 30L;
        }
        ScheduledThreadPoolExecutorUtil.RULE_SCHEDULE.
                scheduleAtFixedRate(new RuleCache(kieTemplate),
                        1, update, TimeUnit.SECONDS);
    }

    @Override
    public void afterPropertiesSet() {

    }


}
