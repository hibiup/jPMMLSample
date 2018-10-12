package samples.jpmml.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

@EnableAutoConfiguration
@Configuration
@PropertySource(value = "classpath:config/config.properties")
public class ApplicationConfiguration {
    private static final Logger logger = LogManager.getLogger(ApplicationConfiguration.class);

    @Bean
    public View jsonView() {
        return new MappingJackson2JsonView();
    }

    @ControllerAdvice
    public static class ControllerExceptionAdvice {
        @ExceptionHandler(value = { Throwable.class })
        public @ResponseBody
        ResponseEntity<Object> onGenericError(final Throwable t,
                                              final HttpServletRequest servletRequest) {
            logger.error(t.getMessage(), t);
            ResponseEntity<Object> entity =  new ResponseEntity(t, HttpStatus.INTERNAL_SERVER_ERROR);
            return entity;
        }

        @ExceptionHandler(value = { IllegalArgumentException.class })
        public @ResponseBody
        ResponseEntity<Object> invalidInputError(final IllegalArgumentException t){
            logger.error(t.getMessage(), t);
            ResponseEntity<Object> entity =  new ResponseEntity(
                    new HashMap<String, Object>() { { put("Error", t.getMessage()); }},
                    HttpStatus.BAD_REQUEST);
            return entity;
        }
    }
}
