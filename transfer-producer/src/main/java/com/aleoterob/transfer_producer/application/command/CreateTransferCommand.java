package com.aleoterob.transfer_producer.application.command;

import java.math.BigDecimal;

public record CreateTransferCommand(
		String fromAccount,
		String toAccount,
		BigDecimal amount,
		String currency) {
}
