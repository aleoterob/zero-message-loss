package com.aleoterob.transfer_producer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransferProducerApplicationTests {

	@Test
	void applicationCanBeCreated() {
		assertThat(new TransferProducerApplication()).isNotNull();
	}

}
