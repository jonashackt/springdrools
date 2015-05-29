package de.jonashackt.springdrools.configuration;

import java.io.IOException;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class DroolsConfiguration {

	private static final String RULES_PATH = "rules/";

	@Bean
	public KieServices kieServices() {
		return KieServices.Factory.get();
	}
	
	@Bean
	public KieFileSystem kieFileSystem() throws IOException {
		KieFileSystem kieFileSystem = kieServices().newKieFileSystem();
		
		Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath*:" + RULES_PATH + "**/*.*");
		
		for (Resource file : files) {
			kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
		}
		
		return kieFileSystem;
	}
	
	@Bean
	public KieContainer kieContainer() throws IOException {
		KieRepository kieRepository = kieServices().getRepository();
		
		kieRepository.addKieModule(new KieModule() {
			@Override
			public ReleaseId getReleaseId() {
				return kieRepository.getDefaultReleaseId();
			}
		});
		
		KieBuilder kieBuilder = kieServices().newKieBuilder(kieFileSystem()); 
		kieBuilder.buildAll(); // KieModule seems to bee automatically added
		
		return kieServices().newKieContainer(kieRepository.getDefaultReleaseId());
	}
	
	@Bean
	public KieBase kieBase() throws IOException {
		return kieContainer().getKieBase();
	}
	
	@Bean
	public KieSession kieSession() throws IOException {
		return kieContainer().newKieSession();
	}
	
	/*
	 *  As http://docs.jboss.org/drools/release/6.2.0.CR1/drools-docs/html/ch.kie.spring.html
	 *  mentions: Without the org.kie.spring.KModuleBeanFactoryPostProcessor bean definition,
	 *  the kie-spring integration will not work
	 */
	@Bean
	public KModuleBeanFactoryPostProcessor kiePostProcessor() {
		return new KModuleBeanFactoryPostProcessor();
	}
}
