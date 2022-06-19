package com.peykasa.authserver.editor;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
@ConfigurationPropertiesBinding
public class DurationConverter implements Converter<String, Duration> {
    @Override
    public Duration convert(String source) {
        if (source == null) {
            return null;
        }
        return Duration.ofMillis(new TimeConfig(source).getMillis());
    }
}