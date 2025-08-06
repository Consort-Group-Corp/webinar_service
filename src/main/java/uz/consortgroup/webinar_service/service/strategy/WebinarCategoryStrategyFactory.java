package uz.consortgroup.webinar_service.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebinarCategoryStrategyFactory {

    private final Map<String, WebinarCategoryStrategy> strategies;

    public WebinarCategoryStrategy getStrategy(String category) {
        if (category == null || category.isBlank()) {
            return strategies.get("planned");
        }

        WebinarCategoryStrategy strategy = strategies.get(category.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported webinar category: " + category);
        }

        return strategy;
    }
}
