package org.holicc.drools.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


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

}
