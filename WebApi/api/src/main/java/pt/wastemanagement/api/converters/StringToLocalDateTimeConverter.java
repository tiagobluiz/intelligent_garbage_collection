package pt.wastemanagement.api.converters;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    @Override
    public LocalDateTime convert(String source) {
        return LocalDateTime.parse(source);
    }
}
