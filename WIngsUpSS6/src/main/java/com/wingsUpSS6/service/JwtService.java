package com.wingsUpSS6.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtService { //OR JwtUtil

    public static final String SECRET = "5367566859703373367639792F423F4528482B4D625165546857605A71347"; //greater than 256 bits (*8)
    public static final long EXPIRATION = 900000; // 15 minutes

    //Decoding/Parsing
    public Claims extractAllClaims(String token) {
    	return Jwts
    			.parser()
    			.setSigningKey(SECRET)
    			.parseClaimsJws(token)
    			.getBody();
    }
    
    public String extractUsername(String token) {
    	Claims claims = extractAllClaims(token);
    	return claims.getSubject();
    }
    
    public Date extractExpiry(String token) {
    	Claims claims = extractAllClaims(token);
    	return claims.getExpiration();
    }
    
    //Encoding/Generation
    public String generateToken(String username) {
    	return Jwts
    			.builder()
    			.signWith(SignatureAlgorithm.HS256, SECRET)
    			.addClaims(new HashMap<>())
    			.setSubject(username)
    			.setIssuedAt(new Date(System.currentTimeMillis()))
    			.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
    			.compact();
    }
    
    //Validation
    public boolean validateToken(String token, UserDetails user) {
    	String username = extractUsername(token);
    	Date expiry = extractExpiry(token);
    	return username.equals(user.getUsername()) && expiry.after(new Date(System.currentTimeMillis()));
    }
    
    
    //Testing
    public static void main(String[] args) {
		UserDetails user = new User("rahul", "lodhi123", List.of(new SimpleGrantedAuthority("ADMIN")));
    	//UserDeatils is interface and User is class, and User class implements UserDetails interface
    	
		JwtService jwtService = new JwtService();
		
    	String token = jwtService.generateToken(user.getUsername());
		System.out.println(token);
		
		Claims claims = jwtService.extractAllClaims(token);
		System.out.println(claims);
		
		Date expiry = jwtService.extractExpiry(token);
		System.out.println(expiry);
		
		String username = jwtService.extractUsername(token);
		System.out.println(username);
		
		boolean validateToken = jwtService.validateToken(token, user);
		System.out.println(validateToken);
	}
}


