package org.holicc.drools;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class FactsProxy {

    private final FactsService factsService;

    private boolean stop = false;

    private Map<String, Object> facts = new ConcurrentHashMap<>();

    public FactsProxy(FactsService factsService) {
        this.factsService = factsService;
    }

    public Object get(String name) {
        Object o = Optional.ofNullable(facts.get(name))
                .orElseGet(() -> factsService.get(facts,name));
        if (log.isDebugEnabled()) {
            log.debug("get value by name:[{}] value:[{}]", name, o);
        }
        return o;
    }


    public void putFact(Object f) {
        if (f instanceof Map) {
            ((Map) f).forEach((k, v) -> {
                if (Objects.nonNull(k) && Objects.nonNull(v)) {
                    this.facts.put(k.toString(), v);
                }
            });
        } else {
            for (Field field : f.getClass().getFields()) {
                field.setAccessible(true);
                try {
                    this.facts.put(field.getName(), field.get(f));
                } catch (Exception e) {
                    //should not happen
                }
            }
        }
    }

    public void put(String key, Object value) {
        this.facts.put(key, value);
    }

    public Map<String, Object> getFacts(){
        return this.facts;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void throwException(String msg) throws Exception {
        throw new Exception(msg);
    }

    public Map<String,Object> doService(String serviceObj, String key) {
        try {
            return factsService.doService(serviceObj, this.facts.get(key));
        } catch (Exception e) {
            log.error("do service failed !", e);
            return null;
        }
    }

    public Map<String, Object> doMark(String key, Object values){
        try {
            return factsService.doMark(key, values);
        } catch (Exception e) {
            log.error("do service failed !", e);
            return null;
        }
    }
}
