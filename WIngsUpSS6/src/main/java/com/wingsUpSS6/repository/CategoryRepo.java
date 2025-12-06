package com.wingsUpSS6.repository;

//import com.fresco.ecommerce.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wingsUpSS6.models.Category;

import java.util.Optional;

// Import required annotations to make use of the Repository
public interface CategoryRepo extends JpaRepository<Category, Integer> {
	Optional<Category> findByCategoryName(String category);
}