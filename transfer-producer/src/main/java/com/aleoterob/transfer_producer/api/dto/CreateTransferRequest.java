package com.aleoterob.transfer_producer.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransferRequest(
		@NotBlank String fromAccount,
		@NotBlank String toAccount,
		@NotNull @DecimalMin("0.01") BigDecimal amount,
		@NotBlank String currency) {
}
