package com.aleoterob.transfer_producer.adapters.output.serialization;

import com.aleoterob.transfer.proto.TransferEvent;
import com.aleoterob.transfer_producer.application.ports.output.TransferEventSerializer;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProtobufTransferEventSerializer implements TransferEventSerializer {
	@Override
	public byte[] serializeCreated(Transfer transfer) {
		// NOTE: Build Protobuf event.
		return TransferEvent.newBuilder()
				.setEventId(UUID.randomUUID().toString())
				.setTransferId(transfer.getId().toString())
				.setFromAccount(transfer.getFromAccount())
				.setToAccount(transfer.getToAccount())
				.setAmount(transfer.getAmount().toPlainString())
				.setCurrency(transfer.getCurrency())
				.setStatus(transfer.getStatus())
				.setCreatedAt(Instant.now().toEpochMilli())
				.build()
				.toByteArray();
	}
}
