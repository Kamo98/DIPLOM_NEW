package ru.vkr.vkr.config;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.vkr.vkr.domain.problem.ProblemFactory;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public ProblemFactory getProblemFactory() {
        return new ProblemFactory();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/student").setViewName("student");
        registry.addViewController("/admin").setViewName("admin");
    }
}
