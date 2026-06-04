package com.aleoterob.transfer_consumer.adapters.input.kafka;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_consumer.application.model.TransferEventPayload;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

final class TransferEventPayloadParser {
	private TransferEventPayloadParser() {
	}

	static TransferEventPayload parse(byte[] payload) throws InvalidProtocolBufferException {
		TransferEvent event = parseProtobuf(payload);
		return new TransferEventPayload(
				UUID.fromString(event.getEventId()),
				UUID.fromString(event.getTransferId()),
				event.getFromAccount(),
				event.getToAccount(),
				new BigDecimal(event.getAmount()),
				event.getCurrency(),
				event.getStatus(),
				event.getCreatedAt());
	}

	private static TransferEvent parseProtobuf(byte[] payload) throws InvalidProtocolBufferException {
		try {
			return TransferEvent.parseFrom(payload);
		} catch (InvalidProtocolBufferException rawPayloadException) {
			String encodedPayload = new String(payload, StandardCharsets.UTF_8).trim();
			if (!isBase64Payload(encodedPayload)) {
				throw rawPayloadException;
			}
			try {
				byte[] decodedPayload = Base64.getDecoder().decode(encodedPayload);
				return TransferEvent.parseFrom(decodedPayload);
			} catch (IllegalArgumentException | InvalidProtocolBufferException encodedPayloadException) {
				rawPayloadException.addSuppressed(encodedPayloadException);
				throw rawPayloadException;
			}
		}
	}

	private static boolean isBase64Payload(String payload) {
		return !payload.isBlank() && payload.length() % 4 == 0 && payload.matches("[A-Za-z0-9+/]+={0,2}");
	}
}
