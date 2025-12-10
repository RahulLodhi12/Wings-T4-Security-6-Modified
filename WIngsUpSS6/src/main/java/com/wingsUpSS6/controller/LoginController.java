package com.wingsUpSS6.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wingsUpSS6.dto.AuthRequest;
import com.wingsUpSS6.dto.JwtResponse;
import com.wingsUpSS6.service.JwtService;

@RestController
@RequestMapping("/api/public")
public class LoginController {
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthenticationManager authenticationManager; // OR AuthenticationProvider authenticationProvider;
	
	@PostMapping("/login")
	public ResponseEntity<Object> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
//		Object or ? are generic type means any data-type 

		try {
			//1. Fetch User by username - This don't validate the password
			UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
			
			//2. Validation of username + password
			UsernamePasswordAuthenticationToken authTokenObject = new UsernamePasswordAuthenticationToken(authRequest.getUsername(),authRequest.getPassword());
			authenticationManager.authenticate(authTokenObject);
			
			//3. Generate Token
			String token = jwtService.generateToken(userDetails.getUsername());


			//4. Return the JWT Response
			JwtResponse jwtResponse = new JwtResponse(token,201);
			
			return ResponseEntity.ok(jwtResponse);
			
		} catch (BadCredentialsException e) {
			
			return ResponseEntity.status(401).body("Invalid username or password");
		}

	}
	
}
