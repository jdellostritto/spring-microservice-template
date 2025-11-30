package com.flipfoundry.tutorial.application.utils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
/**
 * YAML Property Source Factory.
 *
 * <p>An add-on to allow properties to be pulled from an independent YAML file.</p>
 *
 * <p>References: <a href="https://www.baeldung.com/spring-yaml-propertysource">Baeldung - Spring YAML PropertySource</a></p>
 *
 * @author <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 * @since 2025-04-05
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
        var factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        var properties = factory.getObject();

        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }

    
}
