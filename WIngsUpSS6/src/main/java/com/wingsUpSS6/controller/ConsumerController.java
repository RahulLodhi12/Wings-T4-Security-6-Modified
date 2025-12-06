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
@PreAuthorize("hasAuthority('CONSUMER')") //it will apply to every method inside that controller/service.
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
	
//	@PreAuthorize("hasAuthority('CONSUMER')")
	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(@RequestHeader("Authorization") String jwt){
		String username = extractUsernameFromToken(jwt);
		Optional<Cart> cart = cartRepo.findByUserUsername(username);
		if (cart.isPresent()) {
			return ResponseEntity.ok(cart.get());
		}
		return ResponseEntity.notFound().build();
	}
	

//	@PreAuthorize("hasAuthority('CONSUMER')")
	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(@RequestHeader("Authorization") String jwt, @RequestBody Product product){
		String username = extractUsernameFromToken(jwt);
		Optional<UserInfo> user = userRepo.findByUsername(username);
		Optional<Product> dbProduct = productRepo.findById(product.getProductId());
		
		if (user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
		if(dbProduct.isEmpty()) return ResponseEntity.badRequest().body("Product not found");
		
		Optional<Cart> cartOpt = cartRepo.findByUserUsername(username);
	
		
		Cart cart;

		if (cartOpt.isPresent()) {
		    cart = cartOpt.get();
		} else {
		    Cart newCart = new Cart();
		    newCart.setUser(user.get());
		    cart = cartRepo.save(newCart);
		}

		
		// check duplicate for product -> CartId + ProductId 
	    Optional<CartProduct> existing = cpRepo.findByCartUserUserIdAndProductProductId(cart.getCartId(), dbProduct.get().getProductId());

	    if (existing.isPresent()) {
	        return ResponseEntity.status(409).body("Product already exists in cart");
	    }
		
		CartProduct cp = new CartProduct();
		cp.setCart(cart);
		cp.setProduct(dbProduct.get());
		cp.setQuantity(1);
		cpRepo.save(cp);
		
		return ResponseEntity.status(200).body("Product added to cart");
	}
	
	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(@RequestHeader("Authorization") String jwt, @RequestBody CartProduct cp){
		String username = extractUsernameFromToken(jwt);
	    Optional<UserInfo> user = userRepo.findByUsername(username);
	    if (user.isEmpty()) return ResponseEntity.badRequest().body("User not found");

	    // Fetch the cart for this user
	    Optional<Cart> cartOpt = cartRepo.findByUserUsername(username);
	    if (cartOpt.isEmpty()) return ResponseEntity.badRequest().body("Cart not found");

	    Cart cart = cartOpt.get();

	    // Fetch product from DB
	    Optional<Product> productOpt = productRepo.findById(cp.getProduct().getProductId());
	    if (productOpt.isEmpty()) return ResponseEntity.badRequest().body("Product not found");

	    Product product = productOpt.get();

	    // Find existing CartProduct for this user + product
	    Optional<CartProduct> existing = cpRepo.findByCartUserUserIdAndProductProductId(
	            user.get().getUserId(), product.getProductId());

	    if (existing.isPresent()) {
	        CartProduct existingCP = existing.get();

	        if (cp.getQuantity() == 0) {
	            cpRepo.delete(existingCP);
	            return ResponseEntity.ok("Product removed from cart");
	        }

	        existingCP.setQuantity(cp.getQuantity());
	        cpRepo.save(existingCP);
	        return ResponseEntity.ok("Cart updated");
	    }
	    else {
	    	CartProduct newCP = new CartProduct();
	        newCP.setCart(cart);
	        newCP.setProduct(cp.getProduct());
	        newCP.setQuantity(cp.getQuantity());
	        cpRepo.save(newCP);
	        return ResponseEntity.ok("New Product added to cart");
	    }
	    
	}
	
	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(@RequestHeader("Authorization") String jwt, @RequestBody Product product){
		String username = extractUsernameFromToken(jwt);
		Optional<UserInfo> user = userRepo.findByUsername(username);
		if (user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
		
		//combo -> user_id + product_id
		cpRepo.deleteByCartUserUserIdAndProductProductId(user.get().getUserId(), product.getProductId());
		return ResponseEntity.ok("Product removed from cart");
	}
	
	public String extractUsernameFromToken(String jwt) {
		if(jwt!=null && jwt.startsWith("Bearer ")) {
			String token = jwt.substring(7);
			String username = jwtService.extractUsername(token);
			return username;
		}
		return null;
	}
}


