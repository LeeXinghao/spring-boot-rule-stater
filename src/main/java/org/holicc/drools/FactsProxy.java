package org.holicc.drools;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class FactsProxy {

    private final FactsService factsService;

    private boolean stop = false;

    private final Map<String, Object> facts = new ConcurrentHashMap<>();

    public FactsProxy(FactsService factsService) {
        this.factsService = factsService;
    }

    /**
     * 通过该方法获取表达式中的变量
     */
    private <T> T get(String name, Class<T> type) {
        Object o = Optional.ofNullable(factsService.get(name)
        ).orElse(facts.get(name));
        if (log.isDebugEnabled()) {
            log.debug("get value by name:[{}] value:[{}]", name, o);
        }

        return (T) o;
    }

    public Object get(String name) {
        Object o = Optional.ofNullable(facts.get(name))
                .orElseGet(() -> factsService.get(name));
        if (log.isDebugEnabled()) {
            log.debug("get value by name:[{}] value:[{}]", name, o);
        }
        return o;
    }

    public String getString(String name) {
        return get(name, String.class);
    }

    public boolean getBoolean(String name) {
        return get(name, Boolean.class);
    }

    public Collection<?> getCollection(String name) {
        return (Collection<?>) Optional.ofNullable(factsService.get(name)
        ).orElse(facts.get(name));
    }

    public boolean matches(String name, String regex) {
        return getString(name).matches(
                Optional.ofNullable(getString(regex)).orElse(regex));
    }

    public boolean contain(String name, String keyword) {
        return getString(name).contains(
                Optional.ofNullable(getString(keyword)).orElse(keyword));
    }

    public boolean eq(String k1, String k2) {
        return get(k1, Object.class) == get(k2, Object.class)
                || get(k1, Object.class).equals(get(k2, Object.class));
    }

    public boolean in(String collection, String k) {
        return get(collection, Collection.class).contains(get(k, Object.class));
    }

    public boolean empty(String col) {
        return get(col, Collection.class).isEmpty();
    }

    public void putFacts(Object f) {
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

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void throwException(String msg) throws Exception {
        throw new Exception(msg);
    }
}
