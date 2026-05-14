package com.aleoterob.transfer_consumer.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
	private static final long RETRY_INTERVAL_MILLIS = 2000L;
	private static final long MAX_RETRY_ATTEMPTS = 3L;

	@Bean
	public DefaultErrorHandler errorHandler(KafkaOperations<String, byte[]> kafkaOperations) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaOperations,
				(record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
		return new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MILLIS, MAX_RETRY_ATTEMPTS));
	}
}
