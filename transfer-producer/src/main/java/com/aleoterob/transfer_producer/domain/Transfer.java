package com.aleoterob.transfer_producer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "from_account", nullable = false, length = 64)
	private String fromAccount;

	@Column(name = "to_account", nullable = false, length = 64)
	private String toAccount;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 8)
	private String currency;

	@Column(nullable = false, length = 32)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Transfer() {
	}

	public static Transfer create(String fromAccount, String toAccount, BigDecimal amount, String currency, String status) {
		Transfer transfer = new Transfer();
		transfer.id = UUID.randomUUID();
		transfer.fromAccount = fromAccount;
		transfer.toAccount = toAccount;
		transfer.amount = amount;
		transfer.currency = currency;
		transfer.status = status;
		transfer.createdAt = Instant.now();
		return transfer;
	}

	public UUID getId() {
		return id;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
