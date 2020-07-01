package ru.vkr.vkr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import ru.vkr.vkr.components.CustomLogoutSuccessHandler;
import ru.vkr.vkr.components.MySimpleUrlAuthenticationSuccessHandler;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.service.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private MySimpleUrlAuthenticationSuccessHandler mySimpleUrlAuthenticationSuccessHandler;
    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/css/**", "/fonts/**", "/img/**", "/js/**", "/tmp/**", "/error/**");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .authorizeRequests()
                //Доступ только для пользователей с ролью Администратор
                .antMatchers("/admin/**", "/").hasRole("ADMIN")
                //Доступ только для пользователей с ролью Преподаватель
                .antMatchers("/teacher/**").hasRole("TEACHER")
                //Доступ только для пользователей с ролью Студент
                .antMatchers("/student/**").hasRole("STUDENT")
                //Доступ только для пользователей с ролью Студент
                .antMatchers("/user/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .anyRequest().authenticated()

                .and()
                //Настройка для входа в систему
                .formLogin()
                .loginPage("/login")
                .successHandler(mySimpleUrlAuthenticationSuccessHandler)
                .permitAll()

                .and()
                .logout()
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .deleteCookies("remember-me")
                .permitAll()

                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)

                .and()
                .rememberMe()
                .alwaysRemember(true)
                .tokenValiditySeconds(16200)

                .and()
                .sessionManagement()
                .sessionFixation().migrateSession()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login.html")
                .maximumSessions(5)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login.html")
                .sessionRegistry(sessionRegistry());
    }

    @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new MyHttpSessionEventPublisher();
    }

    // Стандартная Spring имплементация SessionRegistry
    @Bean(name = "sessionRegistry")
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
