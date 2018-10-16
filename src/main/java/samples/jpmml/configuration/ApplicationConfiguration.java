package samples.jpmml.configuration;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.concurrent.Executor;

@EnableAutoConfiguration
@Configuration
@PropertySource(value = "classpath:config/config.properties")
@EnableAsync
public class ApplicationConfiguration implements AsyncConfigurer {
    private static final Logger logger = LogManager.getLogger(ApplicationConfiguration.class);

    @Value("${thread.pool.max_size}") int threadPoolMaxSize;
    @Value("${thread.pool.min_size}") int threadPoolMinSize;
    @Value("${thread.pool.capacity}") int threadPoolCapacity;

    @Bean
    public View jsonView() {
        return new MappingJackson2JsonView();
    }

    @Bean
    Executor getExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolMinSize);
        executor.setMaxPoolSize(threadPoolMaxSize);
        executor.setQueueCapacity(threadPoolCapacity);
        executor.initialize();
        return executor;
        //return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public Executor getAsyncExecutor() {
        return getExecutor();
    }

    /*@Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler{
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            logger.debug("");
        }
    }*/

    @ControllerAdvice
    public static class ControllerExceptionAdvice{
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

        @ExceptionHandler(value = { FileAlreadyExistsException.class })
        public @ResponseBody
        ResponseEntity<Object> modelExistsError(final FileAlreadyExistsException t){
            logger.error(t.getMessage(), t);
            ResponseEntity<Object> entity =  new ResponseEntity(
                    new HashMap<String, Object>() { { put("Error", t.getMessage()); }},
                    HttpStatus.CONFLICT);
            return entity;
        }

        /*@ExceptionHandler(value = { FileNotFoundException.class })
        public @ResponseBody
        ResponseEntity<Object> modelNotFoundError(final FileNotFoundException t){
            logger.error(t.getMessage(), t);
            ResponseEntity<Object> entity =  new ResponseEntity(
                    new HashMap<String, Object>() { { put("Error", t.getMessage()); }},
                    HttpStatus.GONE);
            return entity;
        }*/
    }
}
