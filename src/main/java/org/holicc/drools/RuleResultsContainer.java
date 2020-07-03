package org.holicc.drools;

import java.util.Map;

public class RuleResultsContainer {

    private Object result;

    public boolean stop = false;

    public Map<String, String> results;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
