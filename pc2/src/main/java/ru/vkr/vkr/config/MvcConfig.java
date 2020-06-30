package ru.vkr.vkr.config;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.vkr.vkr.domain.problem.ProblemFactory;
import ru.vkr.vkr.service.BridgePc2Service;

import javax.servlet.http.HttpSession;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomInterceptor());
    }

    public static BridgePc2Service getBeanBridgePc2Service (HttpSession session){
        ApplicationContext ctx =
                WebApplicationContextUtils.
                        getWebApplicationContext(session.getServletContext());
        return (ctx != null ? ctx.getBean(BridgePc2Service.class) : null);
    }
}
