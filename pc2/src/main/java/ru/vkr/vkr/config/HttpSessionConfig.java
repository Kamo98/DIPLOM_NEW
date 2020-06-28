package ru.vkr.vkr.config;

import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 60)
public class HttpSessionConfig {
}
