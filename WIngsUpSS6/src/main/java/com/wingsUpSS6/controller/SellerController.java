package com.wingsUpSS6.controller;

import java.net.URI;
//import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wingsUpSS6.models.Category;
import com.wingsUpSS6.models.Product;
import com.wingsUpSS6.models.UserInfo;
import com.wingsUpSS6.repository.CategoryRepo;
import com.wingsUpSS6.repository.ProductRepo;
import com.wingsUpSS6.repository.UserInfoRepository;
import com.wingsUpSS6.service.JwtService;


@RestController
//@PreAuthorize("hasAuthority('SELLER')")
@RequestMapping("/api/auth/seller")
public class SellerController {
	
	@Autowired
	ProductRepo productRepo;
	
	@Autowired
	UserInfoRepository userRepo;
	
	@Autowired
	CategoryRepo categoryRepo;
	
	@Autowired
	JwtService jwtService;
	
	@PreAuthorize("hasAuthority('SELLER')")
	@PostMapping("/product")
	public ResponseEntity<Object> postProduct(@RequestHeader("Authorization") String authHeader, @RequestBody Product product){
		String username = extractUsernameFromToken(authHeader);
	    Optional<UserInfo> user = userRepo.findByUsername(username);
	    
	    Optional<Category> categoryOpt = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());
	    if(categoryOpt.isEmpty()) {
	    	Category category = new Category();
	    	category.setCategoryName(product.getCategory().getCategoryName());
	    	categoryRepo.save(category);
	    }
	    
	    //categoryOpt.isPresent()
	    product.setCategory(categoryOpt.get()); 
	    product.setPrice(product.getPrice());
	    product.setProductId(product.getProductId());
	    product.setProductName(product.getProductName());
	    product.setSeller(user.get());

	    Product savedProduct = productRepo.save(product);

//	    URI location = ServletUriComponentsBuilder
//	            .fromCurrentContextPath()
//	            .path("/api/auth/seller/product/{productId}")
//	            .buildAndExpand(product.getProductId())
//	            .toUri();
//	    ----------------OR--------------
	    String location = "http://localhost:8000/api/auth/seller/product/" + savedProduct.getProductId();


	    return ResponseEntity.created(URI.create(location)).body(savedProduct);
//	    --------------OR---------------
//	    return ResponseEntity
//	            .status(201)             // Set HTTP 201 Created
//	            .header("Location", location)  // Set Location header
//	            .body(savedProduct);     // Include the saved product in body
	    
	}

	@PreAuthorize("hasAuthority('SELLER')")
	@GetMapping("/product")
	public ResponseEntity<Object> getAllProducts(@RequestHeader("Authorization") String authHeader){	
		String username = extractUsernameFromToken(authHeader);
		Optional<UserInfo> user = userRepo.findByUsername(username); //seller
		
		return ResponseEntity.ok(productRepo.findBySellerUserId(user.get().getUserId()));
	}
	
	@PreAuthorize("hasAuthority('SELLER')")
	@GetMapping("/product/{productId}")
	public ResponseEntity<Object> getProduct(@RequestHeader("Authorization") String authHeader, @PathVariable Integer productId){
		String username = extractUsernameFromToken(authHeader);
		Optional<UserInfo> user = userRepo.findByUsername(username); //seller
		
		//combo: user_id + product_id
		Optional<Product> product = productRepo.findBySellerUserIdAndProductId(user.get().getUserId(), productId);
		
		if (product.isPresent()) {
		    return ResponseEntity.ok(product.get());
		} else {
		    return ResponseEntity.status(404).build();
		}
	}
	
	@PreAuthorize("hasAuthority('SELLER')")
	@PutMapping("/product")
	public ResponseEntity<Object> putProduct(@RequestHeader("Authorization") String authHeader, @RequestBody Product updatedProduct){
		String username = extractUsernameFromToken(authHeader);
		Optional<UserInfo> user = userRepo.findByUsername(username); //seller
	
		//combo: user_id + product_id
		Optional<Product> product = productRepo.findBySellerUserIdAndProductId(user.get().getUserId(), updatedProduct.getProductId());
		if(product.isEmpty()) return ResponseEntity.status(404).body("Product not found");
		
		Optional<Category> categoryOpt = categoryRepo.findByCategoryName(updatedProduct.getCategory().getCategoryName());
	    if(categoryOpt.isEmpty()) {
	    	Category category = new Category();
	    	category.setCategoryName(updatedProduct.getCategory().getCategoryName());
	    	categoryRepo.save(category);
	    }
	
		product.get().setCategory(categoryOpt.get());
		product.get().setPrice(updatedProduct.getPrice());
		product.get().setProductId(updatedProduct.getProductId());
		product.get().setProductName(updatedProduct.getProductName());
		product.get().setSeller(updatedProduct.getSeller());
		
		productRepo.save(product.get());
		
		return ResponseEntity.ok().body("Updated..");	
	}
	
	@PreAuthorize("hasAuthority('SELLER')")
	@DeleteMapping("/product/{productId}")
	public ResponseEntity<Product> deleteProduct(@RequestHeader("Authorization") String authHeader, @PathVariable Integer productId){
		String username = extractUsernameFromToken(authHeader);
		Optional<UserInfo> user = userRepo.findByUsername(username); //seller
		
		//combo: user_id + product_id
		Optional<Product> product = productRepo.findBySellerUserIdAndProductId(user.get().getUserId(), productId);
		
		productRepo.deleteById(productId);
		
		return ResponseEntity.ok().build();
	}
	
	public String extractUsernameFromToken(String authHeader) {
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			String username = jwtService.extractUsername(token);
			return username;
		}
		return null;
	}
}
