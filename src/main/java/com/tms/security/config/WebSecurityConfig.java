package com.tms.security.config;

import com.tms.security.filter.CustomWebAuthenticationFilter;
import com.tms.security.provider.WebAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private WebAuthenticationProvider webAuthenticationProvider;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(webAuthenticationProvider);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/assets/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomWebAuthenticationFilter webAuthenticationFilter = new CustomWebAuthenticationFilter(
				authenticationManagerBean());
		webAuthenticationFilter.setFilterProcessesUrl("/tms/auth");
		webAuthenticationFilter.setPostOnly(true);

		http.sessionManagement().invalidSessionUrl("/tms/login")
		.sessionFixation().migrateSession();

		http.headers()
					.defaultsDisabled()
					.frameOptions()
								.deny()
					.httpStrictTransportSecurity()
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)
					.and()
					.xssProtection()
								.block(false)
					.and()
					.cacheControl()
					.and()
					.contentTypeOptions();
		
		http.csrf().disable().antMatcher("/tms/**").authorizeRequests()
				.antMatchers("/tms/login").permitAll()
				.anyRequest().authenticated().and()
				.formLogin();

				
				
		http.addFilterBefore(webAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	}

}
