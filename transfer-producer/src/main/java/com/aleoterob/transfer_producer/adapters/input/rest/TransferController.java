package com.aleoterob.transfer_producer.adapters.input.rest;

import com.aleoterob.transfer_producer.adapters.input.rest.dto.CreateTransferRequest;
import com.aleoterob.transfer_producer.application.command.CreateTransferCommand;
import com.aleoterob.transfer_producer.application.ports.input.CreateTransferUseCase;
import com.aleoterob.transfer_producer.domain.model.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/transfers")
public class TransferController {
	private final CreateTransferUseCase createTransferUseCase;

	public TransferController(CreateTransferUseCase createTransferUseCase) {
		this.createTransferUseCase = createTransferUseCase;
	}

	@PostMapping
	public ResponseEntity<Transfer> create(@RequestBody @Valid CreateTransferRequest request) {
		CreateTransferCommand command = new CreateTransferCommand(
				request.fromAccount(),
				request.toAccount(),
				request.amount(),
				request.currency());

		return ResponseEntity.status(HttpStatus.CREATED).body(createTransferUseCase.create(command));
	}
}
