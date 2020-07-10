package org.holicc.drools;

import java.util.HashMap;
import java.util.Map;

public class RuleResultsContainer {
    public boolean stop = false;

    public Map<String, Object> results = new HashMap<>();

    public Throwable error;
}
