1. Use @PreAuthorize("hasAuthority('CONSUMER')") at class-level, Use @EnableWebSecurity, @EnableMethodSecurity in SecurityConfig at class-level.
   
		After that, we don't need these lines: ".requestMatchers("/api/auth/consumer/**").hasAuthority("CONSUMER")" AND ".requestMatchers("/api/auth/seller/**").hasAuthority("SELLER")"]

2. Use @RequestHeader("Authorization") String jwt in method parameter, instead of Principal object.

		String username = extractUsernameFromToken(authHeader);
		public String extractUsernameFromToken(String authHeader){
			if(authHeader!=null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				String username = jwtService.extractUsername(token);
				return username;
			}
			return null;
		}

3. Not using UserInfoUserDetails implements UserDetails [separate class]. Instead, implements UserDetails in Entity class named "UserInfo". [and override the methods]

4. Not using UserInfoUserDetailsService implements UserDetailsService [separate class]. Instead, create @Bean UserDetailsService in SecurityConfig and we also need to create @Bean of JwtAuthFilter to avoid circular dependency between: SecurityConfig  →  JwtAuthFilter  →  UserDetailsService  →  SecurityConfig. This happens ONLY when you autowire 	JwtAuthFilter inside SecurityConfig, and inside JwtAuthFilter you autowire UserDetailsService.

   			@Bean
			UserDetailsService userDetailsService() {
				return new UserDetailsService() {
			
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
          JwtAuthFilter jwtAuthFilter() {
			  return new JwtAuthFilter();
          }
   -> In SecurityConfig file, Both JwtAuthFilter and UserDetailsService can be @Autowired and @Bean means at a time, Both are @Autowired OR Both are @Bean is fine. But one is @Autowired and one is @Bean is not fine give us circular dependency error.

5. Simple Version of JWTService/JWTUtil/JWTHelper.

6. Add AuthEntryPoint -> If an unauthenticated request tries to access a protected API, call authEntryPoint. So the entry point runs ONLY when authentication fails.

7. Simple version of SecurityConfig, JWTAuthFilter and LoginController.  
