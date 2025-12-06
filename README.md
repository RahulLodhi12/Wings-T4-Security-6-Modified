1. Use @PreAuthorize("hasAuthority('CONSUMER')") at class-level, Use @EnableWebSecurity, @EnableMethodSecurity in SecurityConfig at class-level.
[After that, we don't need these lines: ".requestMatchers("/api/auth/consumer/**").hasAuthority("CONSUMER")" AND ".requestMatchers("/api/auth/seller/**").hasAuthority("SELLER")"]
2. Use @RequestHeader("Authorization") String jwt in method parameter, instead of Principal object.
String username = extractUsernameFromToken(jwt);
public String extractUsernameFromToken(String jwt) {
		if(jwt!=null && jwt.startsWith("Bearer ")) {
			String token = jwt.substring(7);
			String username = jwtService.extractUsername(token);
			return username;
		}
		return null;
	}

3. 
