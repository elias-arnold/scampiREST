package de.scampiRest.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.stereotype.Component;

@Component
public class TomcatContainerCustomizer implements EmbeddedServletContainerCustomizer {

@Override
public void customize(final ConfigurableEmbeddedServletContainer container) {
    if (container instanceof TomcatEmbeddedServletContainerFactory) {
            final TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
            tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
				@Override
				public void customize(Connector connector) {
					// For debugging purpose:
				    connector.setScheme("http"); // production https
				    connector.setProxyPort(8080); // production 443
				}
			});
            
    }
}
}
