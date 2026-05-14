package com.aleoterob.transfer_consumer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransferConsumerApplicationTests {

	@Test
	void applicationCanBeCreated() {
		assertThat(new TransferConsumerApplication()).isNotNull();
	}

}
