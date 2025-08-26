package com.example.inventory.web;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.PriceRule;
import com.example.inventory.dto.PriceValidationResponse;
import com.example.inventory.service.ProductService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

	@Inject
	ProductService service;

	public static class ProductRequest {
		public String name;
		public String sku;
		public String category;
		public java.math.BigDecimal price;
		public String currency;
		public Integer stock;
		public String description;
	}

	public static class ProductResponse {
		public UUID id;
		public String name;
		public String sku;
		public String category;
		public java.math.BigDecimal price;
		public String currency;
		public int stock;
		public String description;
		public java.time.Instant createdAt;
		public java.time.Instant updatedAt;

		public static ProductResponse from(Product p) {
			var r = new ProductResponse();
			r.id = p.getId();
			r.name = p.getName();
			r.sku = p.getSku();
			r.category = p.getCategory();
			r.price = p.getPrice();
			r.currency = p.getCurrency();
			r.stock = p.getStock();
			r.description = p.getDescription();
			r.createdAt = p.getCreatedAt();
			r.updatedAt = p.getUpdatedAt();
			return r;
		}
	}

	@POST
	public Uni<Response> create(ProductRequest req) {
		Product p = map(req);
		return service.create(p).onItem().transform(
				created -> Response.status(Response.Status.CREATED).entity(ProductResponse.from(created)).build());
	}

	@GET
	public Uni<List<ProductResponse>> list() {
		return service.list().onItem().transform(list -> list.stream().map(ProductResponse::from).toList());
	}

	@GET
	@Path("/{id}")
	public Uni<ProductResponse> get(@PathParam("id") UUID id) {
		return service.get(id).onItem().transform(ProductResponse::from);
	}

	@PUT
	@Path("/{id}")
	public Uni<ProductResponse> update(@PathParam("id") UUID id, ProductRequest req) {
		Product update = map(req);
		return service.update(id, update).onItem().transform(ProductResponse::from);
	}

	@DELETE
	@Path("/{id}")
	public Uni<Response> delete(@PathParam("id") UUID id) {
		return service.delete(id).onItem().transform(
				deleted -> deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build());
	}

	@GET
	@Path("/{id}/validate-price")
	public Uni<PriceValidationResponse> validatePrice(@PathParam("id") UUID id) {
		return service.validate(id);
	}

	@GET
	@Path("/rules/{category}")
	public Uni<PriceRule> getRules(@PathParam("category") String category,
			@QueryParam("currency") @DefaultValue("INR") String currency) {
		return service.getRule(category, currency);
	}

	private static Product map(ProductRequest r) {
		Product p = new Product();
		p.setName(r.name);
		p.setSku(r.sku);
		p.setCategory(r.category);
		p.setPrice(r.price);
		p.setCurrency(r.currency);
		p.setStock(r.stock == null ? 0 : r.stock);
		p.setDescription(r.description);
		return p;
	}
}
