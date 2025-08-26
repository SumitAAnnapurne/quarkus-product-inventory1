package com.example.inventory.infra;

import com.example.inventory.domain.Product;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ProductRepository {
	private final Map<UUID, Product> store = new ConcurrentHashMap<>();

	public Uni<Product> create(Product p) {
		var now = Instant.now();
		p.setId(UUID.randomUUID());
		p.setCreatedAt(now);
		p.setUpdatedAt(now);
		store.put(p.getId(), p);
		return Uni.createFrom().item(p);
	}

	public Uni<Optional<Product>> findById(UUID id) {
		return Uni.createFrom().item(Optional.ofNullable(store.get(id)));
	}

	public Uni<List<Product>> findAll() {
		return Uni.createFrom()
				.item(store.values().stream().sorted(Comparator.comparing(Product::getCreatedAt)).toList());
	}

	public Uni<Boolean> delete(UUID id) {
		return Uni.createFrom().item(store.remove(id) != null);
	}

	public Uni<Product> update(Product existing, Product update) {
		existing.setName(update.getName());
		existing.setSku(update.getSku());
		existing.setCategory(update.getCategory());
		existing.setPrice(update.getPrice());
		existing.setCurrency(update.getCurrency());
		existing.setStock(update.getStock());
		existing.setDescription(update.getDescription());
		existing.setUpdatedAt(Instant.now());
		store.put(existing.getId(), existing);
		return Uni.createFrom().item(existing);
		
		// invertory infra changes
	}
}
