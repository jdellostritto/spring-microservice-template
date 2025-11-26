package com.flipfoundry.tutorial.application.config.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

/**
 * 2025 FlipFoundry
 * Class: OpenApiConfig.java
 *
 * @implSpec `<p>This is the Swagger Configuration. This class pulls the
 * the configuration from the OpenApiProps.java class which in turns pulls
 * strings from a language specific property file. This class is not instantiated
 * if not enabled by springdoc.swagger-ui.enabled. For production instances we
 * likely do not want swagger activated.`</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 *
 * @implNote Last modified by Jim DelloStritto on 2025-04-05
 */
@Configuration
@ConditionalOnProperty(value = "springdoc.swagger-ui.enabled", matchIfMissing = false)
@EnableConfigurationProperties(OpenApiProps.class)
public class OpenApiConfig {

  /** The properties. */
  private OpenApiProps props;

  /**
     * Instantiates a new swagger config.
     * @param openApiProps the swagger properties
     */
  @Autowired
  public OpenApiConfig(OpenApiProps openApiProps) {
    this.props = requireNonNull(openApiProps);
  }

    /**
     *
     * @param appVersion
     * @return OpenAPI The Bean that allows us to set custom Properties.
     */
  @Bean
  public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
    return new OpenAPI()
            .info(new Info()
              .contact(new Contact()
                        .name(props.getContactName())
                        .email(props.getContactEmail())
                        .url(props.getContactUrl())
                      )
              .title(props.getTitle())
              .version(props.getVersion())
              .description(props.getDescription())
              .termsOfService(props.getTermsOfServiceUrl())
              .license(new License()
                        .name(props.getLicenseName())
                        .url(props.getLicenseUrl())
                      )
            )
            .externalDocs((new ExternalDocumentation()
                    .description(props.getExternalDocsDescription())
                    .url(props.getExternalDocsUrl())));
  }
}
