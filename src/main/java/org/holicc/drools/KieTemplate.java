package org.holicc.drools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.holicc.drools.config.DroolsProperties;
import org.holicc.drools.util.FileUtil;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionsPool;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.conf.ConsequenceExceptionHandlerOption;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.util.Assert;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.holicc.drools.common.Constants.*;


@Slf4j
public class KieTemplate extends KieAccessor implements BeanClassLoaderAware {

    public Map<String, String> CACHE_RULE = new ConcurrentHashMap<>();

    private final DroolsProperties droolsProperties;

    private ClassLoader classLoader;

    private KieSessionsPool kieSessionsPool;

    public KieTemplate(DroolsProperties droolsProperties) {
        this.droolsProperties = droolsProperties;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    /**
     * 根据文件名获取KieSession
     *
     * @param fileName 文件名，可以输入多个（需要带后缀）
     * @return KieSession
     */
    public KieSession getKieSession(String... fileName) {
        List<String> ds = new ArrayList<>();
        for (String name : fileName) {
            String content = CACHE_RULE.get(name);
            if (StringUtils.isBlank(content)) {
                ds = doReadTemp(fileName);
                return decodeToSession(ds.toArray(new String[]{}));
            }
            ds.add(CACHE_RULE.get(name));
        }
        return decodeToSession(ds.toArray(new String[]{}));
    }

    /**
     * 规则文件，决策表解析成字符串
     *
     * @param realPath 决策表路径
     * @return 字符串
     */
    public String encodeToString(String realPath) {
        File file = new File(realPath);
        if (!file.exists()) {
            return null;
        }
        // drl文件
        if (realPath.endsWith(SUFFIX_DRL)) {
            return read(file);
        }
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error("file not fount.");
        }
        if (realPath.endsWith(SUFFIX_EXCEL) || realPath.endsWith(SUFFIX_EXCEL_2007)) {
            return new SpreadsheetCompiler().compile(is, InputType.XLS);
        }
        // csv文件
        if (realPath.endsWith(SUFFIX_CSV)) {
            return new SpreadsheetCompiler().compile(is, InputType.CSV);
        }
        return null;
    }

    /**
     * 读取drl文件
     */
    private String read(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public StatelessKieSession statelessKieSession(String dsl, String... dslr) {
        //
//        List<KieModuleModel> moduleModels = droolsProperties.getEvaluators().entrySet().stream().map(entry ->
//                KieServices.Factory
//                        .get()
//                        .newKieModuleModel()
//                        .setConfigurationProperty("drools.evaluator." + entry.getKey(), entry.getValue().getName())
//        ).collect(Collectors.toList());
        //
        KieHelper kieHelper = new KieHelper();
        //
//        moduleModels.forEach(kieHelper::setKieModuleModel);
        KieModuleModel kieModuleModel = KieServices.Factory
                .get()
                .newKieModuleModel();
        droolsProperties.getEvaluators().forEach((key, value) -> kieModuleModel.setConfigurationProperty("drools.evaluator." + key, value.getName()));

        kieHelper.setKieModuleModel(kieModuleModel);
        //
        if (StringUtils.isNotBlank(dsl)) {
            kieHelper.addContent(dsl, ResourceType.DSL);
            for (String s : dslr) {
                kieHelper.addContent(s, ResourceType.DSLR);
            }
        } else {
            for (String s : dslr) {
                kieHelper.addContent(s, ResourceType.DRL);
            }
        }
        //
        verify(kieHelper);
        //
        KieBaseConfiguration config = getKieBaseConfiguration(kieHelper);
        //
        return kieHelper.build(config).newStatelessKieSession();
    }

    private KieBaseConfiguration getKieBaseConfiguration(KieHelper kieHelper) {
        KieBaseConfiguration config = kieHelper.ks.newKieBaseConfiguration();
        if ("stream".equalsIgnoreCase(getMode())) {
            config.setOption(EventProcessingOption.STREAM);
        } else {
            config.setOption(EventProcessingOption.CLOUD);
        }
        //
        if (Objects.nonNull(droolsProperties.getExceptionHandler())) {
            config.setOption(ConsequenceExceptionHandlerOption.get(droolsProperties.getExceptionHandler()));
        }
        return config;
    }

    public void verify(KieHelper kieHelper) {
        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
            List<Message> messages = results.getMessages(Message.Level.WARNING, Message.Level.ERROR);
            for (Message message : messages) {
                log.error("Error: {}", message.getText());
            }
            throw new IllegalStateException("Compilation errors [" + messages.stream().map(Message::getText)
                    .reduce(String::concat).get() + "]");
        }
    }

    /**
     * 把字符串解析成KieSession
     *
     * @param file 规则文件字符串
     * @return KieSession
     */
    public KieSession decodeToSession(String... file) {
        KieBase kieBase = getKieBase(file);
        return kieBase.newKieSession();
    }

    private KieBase getKieBase(String[] file) {
        KieHelper kieHelper = new KieHelper();
        for (String s : file) {
            if (s.endsWith("dsl")) {
                kieHelper.addContent(s, ResourceType.DSL);
            } else if (s.endsWith("drl")) {
                kieHelper.addContent(s, ResourceType.DRL);
            } else if (s.endsWith("dslr")) {
                kieHelper.addContent(s, ResourceType.DSLR);
            }
        }
        return kieHelper.build(getKieBaseConfiguration(kieHelper));
    }

    /**
     * 获取绝对路径下的规则文件对应的KieBase
     *
     * @param classPath 绝对路径/文件目录
     * @return KieBase
     */
    public KieBase getKieBase(String classPath) throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        Resource resource = ResourceFactory.newFileResource(classPath);
        kfs.write(resource);
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        if (kieBuilder.getResults().getMessages(Message.Level.ERROR).size() > 0) {
            throw new Exception();
        }
        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository()
                .getDefaultReleaseId());
        return kieContainer.getKieBase();
    }

    /**
     * 私有，do开头，0结尾的方法全部为私有
     */
    public void doRead0() {
        // 先存入1级缓存
        String pathTotal = getPath();
        if (pathTotal == null || pathTotal.length() == 0) {
            return;
        }
        String[] pathArray = pathTotal.split(PATH_SPLIT);
        List<File> fileList = new ArrayList<>();
        for (String path : pathArray) {
            FileUtil.fileList(path, fileList);
        }
        for (File file : fileList) {
            String fileName = file.getName();
            String content = encodeToString(file.getPath());
            CACHE_RULE.put(fileName, content);
        }
        // 有Redis则存入Redis
        // ....

    }

    private List<String> doReadTemp(String... fileName) {
        // 转换成集合
        List<String> fl = Arrays.asList(fileName);
        // 存放临时规则文件
        List<String> ds = new ArrayList<>();
        // 先存入1级缓存
        String pathTotal = getPath();
        Assert.notNull(pathTotal, "path must be not null");
        String[] pathArray = pathTotal.split(PATH_SPLIT);
        List<File> fileList = new ArrayList<>();
        for (String path : pathArray) {
            FileUtil.fileList(path, fileList);
        }
        for (File file : fileList) {
            if (fl.contains(file.getName())) {
                String content = encodeToString(file.getPath());
                ds.add(content);
                CACHE_RULE.put(file.getName(), content);
            }
        }
        return ds;
    }
}
