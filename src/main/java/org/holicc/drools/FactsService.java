package org.holicc.drools;

import java.util.Map;

public interface FactsService {

    Object get(Map<String, Object> facts, String key);

    Map<String, Object> doService(String serviceName, Object params);

    Map<String, Object> doMark(String key, Object values);
}
