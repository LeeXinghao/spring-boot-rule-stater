package org.holicc.drools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
public class KieAccessor implements InitializingBean {

    public static final String PATH_SPLIT = ",";

    private String path;

    private String mode;

    private Long update;

    @Override
    public void afterPropertiesSet() throws Exception {
     
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Long getUpdate() {
        return update;
    }

    public void setUpdate(Long update) {
        this.update = update;
    }
}
