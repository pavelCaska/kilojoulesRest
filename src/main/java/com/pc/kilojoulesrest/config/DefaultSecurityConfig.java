package com.pc.kilojoulesrest.config;

import com.pc.kilojoulesrest.filter.JwtAuthFilter;
import com.pc.kilojoulesrest.service.UserInfoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {

    @Bean
    UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                   HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests((requests) -> requests
                        .requestMatchers(mvcMatcherBuilder.pattern("/api/login")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/api/isRunning")).hasRole("USER")
                        .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.GET,"/food/{foodId}/portion")).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.POST,"/food/{foodId}/portion")).hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.POST,"/api/item")).hasRole("USER")
//                        .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.GET,"/api/item")).hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(mvcMatcherBuilder.pattern("/api/bid/{itemId}")).hasRole("USER")
//                        .requestMatchers(mvcMatcherBuilder.pattern("/api/user")).hasRole("ADMIN")
//                        .requestMatchers(mvcMatcherBuilder.pattern("/api/balance")).hasRole("ADMIN")
//                        .requestMatchers(mvcMatcherBuilder.pattern("/api/purchase/{id}/delete")).hasRole("ADMIN")
                        .requestMatchers(mvcMatcherBuilder.pattern("/error")).permitAll() // Shouldn't be used with respect for frontend!
                        .anyRequest().authenticated())
//                        .anyRequest().permitAll())
                .csrf((csrf) -> csrf.disable());
        http.authenticationProvider(authenticationProvider())
                .addFilterBefore( jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
