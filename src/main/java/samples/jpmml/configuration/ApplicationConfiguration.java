package samples.jpmml.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@EnableAutoConfiguration
@Configuration
public class ApplicationConfiguration {
    @Bean
    public View jsonView() {
        return new MappingJackson2JsonView();
    }
}
