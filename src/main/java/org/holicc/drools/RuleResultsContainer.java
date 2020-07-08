package org.holicc.drools;

import java.util.HashMap;
import java.util.Map;

public class RuleResultsContainer {

    private Object result;

    public boolean stop = false;

    public Map<String, Object> results = new HashMap<>();

    private Throwable error;

    public void setError(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
