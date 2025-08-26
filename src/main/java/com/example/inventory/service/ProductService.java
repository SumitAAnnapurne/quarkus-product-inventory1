package com.example.inventory.service;

import java.util.List;
import java.util.UUID;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.PriceRule;
import com.example.inventory.dto.PriceValidationRequest;
import com.example.inventory.dto.PriceValidationResponse;
import com.example.inventory.ext.PriceRulesClient;
import com.example.inventory.infra.ProductRepository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductService {

	@Inject
	ProductRepository repo;
	
	@Inject
	PriceRulesClient priceRulesClient;
	
	// create product
	public Uni<Product> create(Product p) {
		return validatePrice(p).onItem().transformToUni(ok -> repo.create(p));
	}
	
	// update the product by id
	public Uni<Product> update(UUID id, Product update) {
		return repo.findById(id).onItem()
				.transform(opt -> opt
						.orElseThrow(() -> new java.util.NoSuchElementException("Product %s not found".formatted(id))))
				.onItem().transformToUni(existing -> validatePrice(update).replaceWith(existing)).onItem()
				.transformToUni(existing -> repo.update(existing, update));
	}
	
	
	public Uni<Product> get(UUID id) {
		return repo.findById(id).onItem().transform(opt -> opt
				.orElseThrow(() -> new java.util.NoSuchElementException("Product %s not found".formatted(id))));
	}
	
	// get all the product
	public Uni<List<Product>> list() {
		return repo.findAll();
	}
	
	// delete product by id
	public Uni<Boolean> delete(UUID id) {
		return repo.delete(id);
	}

	public Uni<PriceRule> getRule(String category, String currency) {
		return priceRulesClient.getRule(category, currency);
	}

	public Uni<PriceValidationResponse> validate(UUID id) {
		return get(id).onItem().transformToUni(p -> validatePrice(p));
	}

	private Uni<PriceValidationResponse> validatePrice(Product p) {
		var req = new PriceValidationRequest();
		req.category = p.getCategory();
		req.currency = p.getCurrency();
		req.price = p.getPrice();
		return priceRulesClient.validate(req).onItem().invoke(resp -> {
			if (!resp.valid)
				throw new IllegalArgumentException(resp.reason == null ? "Price invalid" : resp.reason);
		}).onFailure().transform(t -> new IllegalArgumentException("Price validation failed: " + t.getMessage()));
	}
}
