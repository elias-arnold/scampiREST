package de.scampiRest;

import java.text.SimpleDateFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.scampiRest.applib.ScampiCommunicator;

@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter {
    
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
}
