package org.holicc.drools;

public class RuleResultsContainer {

    private Object result;

    public boolean find() {
        System.out.println("\n i'm hook!");
        return true;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
