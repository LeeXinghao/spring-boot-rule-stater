package org.holicc.drools.config;

import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.kie.api.runtime.rule.ConsequenceExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


@ConfigurationProperties(prefix = "spring.drools")
public class DroolsProperties {

    /**
     * 规则文件和决策表目录，多个目录使用逗号分割
     */
    private String path;

    /**
     * 轮询周期 - 单位：秒
     */
    private Long update;

    /**
     * 模式，stream 或 cloud
     */
    private String mode;

    /**
     * 自定义操作符
     */
    private Map<String, Class<? extends EvaluatorDefinition>> evaluators;

    private Class<? extends ConsequenceExceptionHandler> exceptionHandler;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getUpdate() {
        return update;
    }

    public void setUpdate(Long update) {
        this.update = update;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Map<String, Class<? extends EvaluatorDefinition>> getEvaluators() {
        return evaluators;
    }

    public void setEvaluators(Map<String, Class<? extends EvaluatorDefinition>> evaluators) {
        this.evaluators = evaluators;
    }

    public void setExceptionHandler(Class<? extends ConsequenceExceptionHandler> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public Class<? extends ConsequenceExceptionHandler> getExceptionHandler() {
        return exceptionHandler;
    }
}
