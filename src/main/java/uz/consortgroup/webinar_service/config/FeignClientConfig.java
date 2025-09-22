package uz.consortgroup.webinar_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return this::propagateHeaders;
    }

    private void propagateHeaders(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest req = attrs.getRequest();

        String auth = req.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
            template.header("Authorization", auth);
        }

        copyHeader(req, template, "X-User-Id");
        copyHeader(req, template, "X-User-Email");
        copyHeader(req, template, "X-User-Roles");
        copyHeader(req, template, "X-Auth-Validated");
        copyHeader(req, template, "X-Request-Id");
    }

    private static void copyHeader(HttpServletRequest req, RequestTemplate template, String name) {
        String value = req.getHeader(name);
        if (value != null && !value.isBlank()) {
            template.header(name, value);
        }
    }
}