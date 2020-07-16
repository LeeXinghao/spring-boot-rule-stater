## Spring Boot Drools Starter

Drools规则引擎框架与Spring Boot整合成的Starter


### KieTemplate 

该类用与获取Session来执行规则

```java
StatelessKieSession kieSession = kieTemplate.statelessKieSession(dslCache, dslr);
```

### FactsProxy

该类用与存储规则执行中的变量

```java
FactsProxy factsProxy = this.factsProxyFactory.getObject();
factsProxy.putFact(params);
```

FactsProxy中包含`FactsService` 这个Service包含的是获取变量的业务方法；

FactsProxy中get(key)方法的执行流程：

 1. 尝试从putFact()方法中的变量map中获取值
 2. 如果facts 中有值就返回
 3. 如果获取不到对应的值就从FactService的get方法中获取

```java
public Object get(String name) {
        Object o = Optional.ofNullable(facts.get(name))
                .orElseGet(() -> factsService.get(name));
        if (log.isDebugEnabled()) {
            log.debug("get value by name:[{}] value:[{}]", name, o);
        }
        return o;
    }
```

### RuleResultsContainer

该类是规则执行中的全局变量，用于存储规则执行的结果

```java
RuleResultsContainer container=new RuleResultsContainer();
kieSession.setGlobal("rule", container);
```

```java
public class RuleResultsContainer {
    public boolean stop = false;//是否中断

    public Map<String, Object> results = new HashMap<>();//结果集

    public Throwable error;//异常
}
```

### DroolsRule

该类用于编译规则或分词