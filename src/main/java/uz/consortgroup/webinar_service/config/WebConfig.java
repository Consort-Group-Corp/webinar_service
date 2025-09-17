package uz.consortgroup.webinar_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .findFirst()
                .ifPresent(jackson -> {

                    List<MediaType> types = new ArrayList<>(jackson.getSupportedMediaTypes());

                    if (!types.contains(MediaType.TEXT_PLAIN)) {
                        types.add(MediaType.TEXT_PLAIN);
                    }

                    if (!types.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                        types.add(MediaType.APPLICATION_OCTET_STREAM);
                    }

                    jackson.setSupportedMediaTypes(types);
                    jackson.setDefaultCharset(StandardCharsets.UTF_8);
                });
    }
}

