package com.example.inventory.ext;

import com.example.inventory.dto.PriceRule;
import com.example.inventory.dto.PriceValidationRequest;
import com.example.inventory.dto.PriceValidationResponse;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "price-rules")
@Path("/api/v1/price-rules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PriceRulesClient {
	@GET
	@Path("/rules/{category}")
	Uni<PriceRule> getRule(@PathParam("category") String category, @QueryParam("currency") String currency);

	@POST
	@Path("/validate")
	Uni<PriceValidationResponse> validate(PriceValidationRequest req);
}
