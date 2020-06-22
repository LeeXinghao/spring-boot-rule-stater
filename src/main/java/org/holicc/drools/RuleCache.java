package org.holicc.drools;


public class RuleCache implements Runnable{

    private KieTemplate kieTemplate;

    public RuleCache(KieTemplate kieTemplate) {
        this.kieTemplate = kieTemplate;
    }

    @Override
    public void run() {
        kieTemplate.doRead0();
    }
}
