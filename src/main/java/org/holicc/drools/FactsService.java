package org.holicc.drools;

import java.util.Map;

public interface FactsService {

    Object get(String key);

    Map<String,Object> doService(String serviceName, Object params);
}
