package com.wingsUpSS6.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wingsUpSS6.models.Cart;
import com.wingsUpSS6.models.CartProduct;
import com.wingsUpSS6.models.Product;
import com.wingsUpSS6.models.UserInfo;
import com.wingsUpSS6.repository.CartProductRepo;
import com.wingsUpSS6.repository.CartRepo;
import com.wingsUpSS6.repository.ProductRepo;
import com.wingsUpSS6.repository.UserInfoRepository;
import com.wingsUpSS6.service.JwtService;


@RestController
//@PreAuthorize("hasAuthority('CONSUMER')") //it will apply to every method inside that controller/service.
@RequestMapping("/api/auth/consumer")
public class ConsumerController {
	
	@Autowired
	ProductRepo productRepo;
	
	@Autowired
	CartRepo cartRepo;
	
	@Autowired
	CartProductRepo cpRepo;
	
	@Autowired
	UserInfoRepository userRepo;
	
	@Autowired
	JwtService jwtService;
	
	//authHeader -> [Bearer ncjajajbasjcjascncjcaslcncsnl]
	@PreAuthorize("hasAuthority('CONSUMER')")
	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(@RequestHeader("Authorization") String authHeader){
		String username = extractUsernameFromToken(authHeader);
		Optional<Cart> cart = cartRepo.findByUserUsername(username);
		if (cart.isPresent()) {
			return ResponseEntity.ok(cart.get());
		}
		return ResponseEntity.notFound().build();
	}
	

	@PreAuthorize("hasAuthority('CONSUMER')")
	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(@RequestHeader("Authorization") String authHeader, @RequestBody Product product){
		String username = extractUsernameFromToken(authHeader);
		Optional<Cart> cartOpt = cartRepo.findByUserUsername(username);

		if (cartOpt.isPresent()) {
			
			// check duplicate - user + product
			Optional<CartProduct> cpExist = cpRepo.findByCartUserUserIdAndProductProductId(cartOpt.get().getCartId(),
					product.getProductId());
			if (cpExist.isPresent()) {
				return ResponseEntity.status(409).body("Product already exist in Cart");
			}
		}
		
		CartProduct cp = new CartProduct();
		
		cp.setCart(cartOpt.get());
		cp.setProduct(product);
		cp.setQuantity(1);
		cpRepo.save(cp);

		return ResponseEntity.status(200).body("Product added to cart");
	}
	
	@PreAuthorize("hasAuthority('CONSUMER')")
	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(@RequestHeader("Authorization") String authHeader, @RequestBody CartProduct cp){
		String username = extractUsernameFromToken(authHeader);
		Optional<Cart> cartOpt = cartRepo.findByUserUsername(username);

	    Cart cart = cartOpt.get();

	    // Find existing CartProduct for this user + product
	    Optional<CartProduct> cpExist = cpRepo.findByCartUserUserIdAndProductProductId(
	            cart.getCartId(), cp.getProduct().getProductId());

	    if (cp.getQuantity() == 0) {
	    	cpRepo.delete(cpExist.get());
	    	return ResponseEntity.ok("Product removed from cart");
	    }
	    
	    cpExist.get().setQuantity(cp.getQuantity());
	    cpRepo.save(cpExist.get());
	    return ResponseEntity.ok("Cart updated");
	    
	}
	
	
	@PreAuthorize("hasAuthority('CONSUMER')")
	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(@RequestHeader("Authorization") String authHeader, @RequestBody Product product){
		String username = extractUsernameFromToken(authHeader);
		Optional<UserInfo> user = userRepo.findByUsername(username);
		
		//combo -> user_id + product_id
		cpRepo.deleteByCartUserUserIdAndProductProductId(user.get().getUserId(), product.getProductId());
		return ResponseEntity.ok("Product removed from cart");
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


