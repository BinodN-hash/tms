package com.tms.security.config;

import com.tms.properties.SetUpProperties;
import com.tms.security.filter.CustomRestAuthenticationFilter;
import com.tms.security.filter.JwtRequestFilter;
import com.tms.security.filter.RequestThrottleFilter;
import com.tms.security.provider.RestAuthenticationProvider;
import com.tms.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Order(1)
@Configuration
public class RestSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private RestAuthenticationProvider restAuthenticationProvider;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	@Autowired
	private RequestThrottleFilter requestThrottleFilter;
	
	private SetUpProperties setup = SetUpProperties.getInstance();
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(setup.getProperty("rest.allowedOrigins").split(",")));
		configuration.setAllowedMethods(Arrays.asList(setup.getProperty("rest.allowedMethods").split(",")));
		configuration.setAllowedHeaders(Arrays.asList(setup.getProperty("rest.allowedHeaders").split(",")));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/rest/**", configuration);
		return source;
	}
	
	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> response.sendError(SC_UNAUTHORIZED, authException.getMessage());
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(restAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomRestAuthenticationFilter restAuthenticationFilter = new CustomRestAuthenticationFilter(authenticationManagerBean(), jwtTokenUtil);
		restAuthenticationFilter.setFilterProcessesUrl("/rest/access/login");
		
		http.cors().and().csrf().disable();
		http.sessionManagement().sessionCreationPolicy(STATELESS);
		
		http.headers()
					.defaultsDisabled()
					.frameOptions()
								.sameOrigin()
					.httpStrictTransportSecurity()
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)
					.and()
					.xssProtection()
								.block(false)
					.and()
					.contentSecurityPolicy("default-src 'self' 'unsafe-inline' 'unsafe-eval'; object-src 'self'; script-src 'self' 'unsafe-eval' 'unsafe-inline'; media-src *;font-src 'self' data:; img-src 'self' data:;")
					.and()
					.referrerPolicy(ReferrerPolicy.SAME_ORIGIN)
					.and()
					.cacheControl()
					.and()
					.contentTypeOptions();
		
		http.antMatcher("/rest/**").authorizeRequests()
		
			.antMatchers("/rest/access/login").permitAll()
			.antMatchers("/rest/access/token/refresh").permitAll()

			.anyRequest().authenticated()
			.and()
			.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
		
		// RequestThrottleFilter is before UsernamePasswordAuthenticationFilter.class that's why its not needed to add on WebSecurityConfig
		http.addFilterBefore(requestThrottleFilter, UsernamePasswordAuthenticationFilter.class);
		
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		http.addFilterAt(restAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
