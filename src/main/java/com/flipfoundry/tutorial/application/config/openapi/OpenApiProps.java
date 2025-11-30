package com.flipfoundry.tutorial.application.config.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.flipfoundry.tutorial.application.utils.YamlPropertySourceFactory;


/**
 * OpenAPI Properties Configuration.
 * 
 * <p>This class is conditional and only used if swagger-ui is enabled.
 * The property file loaded is based on the {@code spring.profiles.locale}.
 * The class leverages Lombok @Data annotation to provide getters, setters, etc.</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 * @since 2025-04-05
 */
@Configuration
@ConditionalOnProperty(value = "springdoc.swagger-ui.enabled", matchIfMissing = false)
@ConfigurationProperties(prefix = "openapi.info")
@PropertySource(value = "classpath:locale-${SPRING_PROFILES_LOCALE:en}.yml", factory = YamlPropertySourceFactory.class)
@Data
public class OpenApiProps {

  private String version;
  private String title;
  private String description;
  private String termsOfServiceUrl;
  private String licenseName;
  private String licenseUrl;
  private String contactName;
  private String contactUrl;
  private String contactEmail;
  private String externalDocsDescription;
  private String externalDocsUrl;

}
