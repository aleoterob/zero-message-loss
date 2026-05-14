package com.aleoterob.transfer_consumer.application;

import com.aleoterob.transfer.proto.TransferEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class TransferEventPayload {
	private TransferEventPayload() {
	}

	static TransferEvent parse(byte[] payload) throws InvalidProtocolBufferException {
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
