package de.scampiRest;

import java.io.File;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.scampiRest.applib.ScampiCommunicator;

@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter {
	
	@Value("${multipart.location}")
	private String tempFilePath;
	
	@Value("${scampi.publicStorage}")
	private String storagePath;
	
	@Value("${scampi.mongoPath}")
	private String mongoPath;
    
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
    public static void main(String[] args) {
        @SuppressWarnings("unused")
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
        
        System.out.println("Started");

    }
    
    public Jackson2ObjectMapperBuilder objectMapper() {
		Jackson2ObjectMapperBuilder om = new Jackson2ObjectMapperBuilder();
		om.autoDetectGettersSetters(true);
		om.indentOutput(true).dateFormat(
				new SimpleDateFormat("yyyy-MM-dd"));
		om.failOnUnknownProperties(false);
		return om;
	}
    
    @Bean
    public ScampiCommunicator scampiCommunicator(){
    	return new ScampiCommunicator();
    }
    
    @Bean(name="tempFilePath")
    public String tempFilePath(){
    	try {
			File path = new File(tempFilePath);
			if (!path.exists()){
				path.mkdir();
			}
		} catch (Exception e) {
			logger.error(tempFilePath, e);
		}
    	return this.tempFilePath;
    }
    
    @Bean(name="storagePath") 
    public String storagePath(){
    	try {
			File path = new File(storagePath);
			if (!path.exists()){
				path.mkdir();
			}
		} catch (Exception e) {
			logger.error(storagePath, e);
		}
    	return this.storagePath;
    }
    @Bean(name="mongoPath") 
    public String mongoPath(){
    	try {
			File path = new File(mongoPath);
			if (!path.exists()){
				path.mkdir();
			}
		} catch (Exception e) {
			logger.error(mongoPath, e);
		}
    	return this.mongoPath;
    }
}
