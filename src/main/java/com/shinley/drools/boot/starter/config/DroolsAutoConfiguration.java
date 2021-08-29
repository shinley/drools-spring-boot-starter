package com.shinley.drools.boot.starter.config;

import com.shinley.drools.boot.starter.annotation.InitDroolsMarker;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Configuration
@ConditionalOnBean(InitDroolsMarker.class)
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "drools.rules")
public class DroolsAutoConfiguration {
    private static Logger logger = LoggerFactory.getLogger(DroolsAutoConfiguration.class);

    /**
     * 用户自定义classpath下的规则文件路径
     */
    @Value("${path}")
    private String custom_rules_path;

    /**
     * 默认classpath下的规则文件路么
     */
    private static final String DEFAULT_RULES_PATH = "rules/";

    private final KieServices kieServices = KieServices.Factory.get();

    static {
        System.out.println("init drools spring boot starter.....");
    }

    @Bean
    @ConditionalOnMissingBean
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        if (custom_rules_path == null) {
            custom_rules_path = DEFAULT_RULES_PATH;
            logger.info("not config rule file path, using default rule file path `rules/`");
        }

        Resource[] files = resourcePatternResolver.getResources("classpath*:" + custom_rules_path + "*.*");
        String path = null;
        for (Resource file : files) {
            path = custom_rules_path + file.getFilename();
            kieFileSystem.write(ResourceFactory.newClassPathResource(path, "UTF-8"));
        }
        return kieFileSystem;
    }

    @Bean
    @ConditionalOnMissingBean
    public KieContainer kieContainer() throws IOException {
        KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem());
        kieBuilder.buildAll();
        return kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    }

    @Bean
    @ConditionalOnMissingBean
    public KieBase kieBase() throws IOException {
        return kieContainer().getKieBase();
    }

    @Bean
    @ConditionalOnMissingBean
    public KModuleBeanFactoryPostProcessor kiePostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }

}
