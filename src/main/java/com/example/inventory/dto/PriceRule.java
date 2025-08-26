package com.example.inventory.dto;

import java.math.BigDecimal;

public class PriceRule {
	public String category;
	public String currency;
	public BigDecimal min;
	public BigDecimal max;
	public BigDecimal recommended;
}
