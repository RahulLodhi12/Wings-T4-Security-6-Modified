package com.wingsUpSS6.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

//import com.wings.config.UserInfoUserDetailsService;
import com.wingsUpSS6.filter.JwtAuthFilter;
import com.wingsUpSS6.models.UserInfo;
import com.wingsUpSS6.repository.UserInfoRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Autowired
	private AuthEntryPoint authEntryPoint;
	
	@Autowired
	private UserInfoRepository repository;
	
	
//	@Autowired
//	private JwtAuthFilter authFilter;
	
//	@Autowired
//	private UserInfoUserDetailsService userInfoUserDetailsService;
	
//	-----XXX-- OR --XXX------
	
	@Bean
	JwtAuthFilter jwtAuthFilter() {
		return new JwtAuthFilter();
	}
	
	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() { //Anonymous Inner Class
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				Optional<UserInfo> userInfo = repository.findByUsername(username);

		        if(userInfo.isPresent()){
		            return new UserInfo(userInfo.get().getUsername(),userInfo.get().getPassword(),userInfo.get().getRoles()); //We can't directly return object of UserDetails, since UserDetails is an interface.
		        }
		        else{
		            throw new UsernameNotFoundException("User Not Found..");
		        }
			}
		};
	}

	
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/h2-console/**");
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		
		//step 1. disable csrf
		http.csrf(csrf -> csrf.disable());
		//step 2: change the session management policy
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		//step 3: register custom filter [JWTAuthFilter]
		http.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
		//step 4: register url's
		http.authenticationProvider(authenticationProvider());
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/public/**").permitAll()
//				.requestMatchers("/api/auth/consumer/**").hasAuthority("CONSUMER")
//				.requestMatchers("/api/auth/seller/**").hasAuthority("SELLER")
				.anyRequest().authenticated());

		//add exception handler
		http.exceptionHandling(ex->ex.authenticationEntryPoint(authEntryPoint)); //“If an unauthenticated request tries to access a protected API, call authEntryPoint.”So the entry point runs ONLY when authentication fails.
		
     return http.build();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	    provider.setUserDetailsService(userDetailsService());
	    provider.setPasswordEncoder(passwordEncoder());
	    return provider;
	}
	
	
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
//		return null;
		return config.getAuthenticationManager();
	}

}
