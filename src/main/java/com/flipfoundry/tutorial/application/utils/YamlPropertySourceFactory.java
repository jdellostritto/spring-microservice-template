package com.flipfoundry.tutorial.application.utils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
/**
 * 
 * 2025 FlipFoundry.
 *
 * Class:      YamlPropertySourceFactory.java<br>
 * References: `https://www.baeldung.com/spring-yaml-propertysource`<br><br>
 *
 * @implSpec `An add-on to allow properties to be pulled form an independent yaml file.`<br>
 *
 * @author <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 * @implNote Last modified by Jim DelloStritto on 2025-04-05
 *
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
