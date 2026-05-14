package com.aleoterob.transfer_producer.api;

import com.aleoterob.transfer_producer.api.dto.CreateTransferRequest;
import com.aleoterob.transfer_producer.application.TransferService;
import com.aleoterob.transfer_producer.domain.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/transfers")
public class TransferController {
	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	@PostMapping
	public ResponseEntity<Transfer> create(@RequestBody @Valid CreateTransferRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(request));
	}
}
