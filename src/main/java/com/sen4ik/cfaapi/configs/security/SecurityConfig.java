package com.sen4ik.cfaapi.configs.security;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.enums.AuthPaths;
import com.sen4ik.cfaapi.enums.UserPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    static String apiPrefix = Constants.API_PREFIX;

    static String[] permitAllGetPatterns = {
            apiPrefix + "/tag/**",
            apiPrefix + "/category/**",
            apiPrefix + "/file/**"
    };

    static String[] adminGetPatterns = {
            apiPrefix + "/user/all",
            apiPrefix + "/user/me",
            apiPrefix + "/playlist/all",
            apiPrefix + "/everything/**"
    };

    static String[] userGetPatterns = {
            apiPrefix + "/playlist/user/**",
            apiPrefix + "/playlist/**",
            apiPrefix + "/user/me"
    };

    static String[] adminDeletePatterns = {
            apiPrefix + "/tag/**",
            apiPrefix + "/user/**",
            apiPrefix + "/playlist/**",
            apiPrefix + "/playlist/**/file/**"
    };

    static String[] adminPostPatterns = {
            apiPrefix + "/tag/**",
            apiPrefix + "/playlist/add",
            apiPrefix + "/playlist/**/file/**",
            apiPrefix + "/user/**"
    };

    static String[] userDeletePatterns = {
            apiPrefix + "/playlist/**",
            apiPrefix + "/playlist/**/file/**",
    };

    static String[] userPostPatterns = {
            apiPrefix + "/user/**",
            apiPrefix + "/playlist/add",
            apiPrefix + "/playlist/**/file/**",
    };

    static String[] swagger = {
            "/swagger/docs",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //@formatter:off
        http
            .httpBasic().disable()
            .csrf().disable()
            // .cors().disable() // CORS is now controlled by WebConfig
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(   apiPrefix + AuthPaths.signIn.value,
                                            apiPrefix + UserPaths.signUp.value).permitAll()
                .antMatchers(HttpMethod.GET, permitAllGetPatterns).permitAll()
                .antMatchers(HttpMethod.GET, adminGetPatterns).hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.GET, userGetPatterns).hasAnyRole("ADMIN", "USER")
                .antMatchers(HttpMethod.DELETE, adminDeletePatterns).hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, userDeletePatterns).hasAnyRole("ADMIN", "USER")
                .antMatchers(HttpMethod.POST, adminPostPatterns).hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.POST, userPostPatterns).hasAnyRole("ADMIN", "USER")
                .antMatchers(swagger).permitAll()
                .anyRequest().authenticated()
            .and()
            .apply(new JwtSecurityConfigurer(jwtTokenProvider));
        //@formatter:on
    }

}

