package com.aleoterob.transfer_consumer.application;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class ConsumerControlService {
	public static final String TRANSFER_LISTENER_ID = "transfer-created-listener";

	private final KafkaListenerEndpointRegistry listenerEndpointRegistry;
	private volatile boolean failProcessing;

	public ConsumerControlService(KafkaListenerEndpointRegistry listenerEndpointRegistry) {
		this.listenerEndpointRegistry = listenerEndpointRegistry;
	}

	public ConsumerStatus status() {
		MessageListenerContainer container = transferListenerContainer();
		return new ConsumerStatus(container != null && container.isContainerPaused(), failProcessing);
	}

	public void pause() {
		MessageListenerContainer container = transferListenerContainer();
		if (container != null) {
			container.pause();
		}
	}

	public void resume() {
		MessageListenerContainer container = transferListenerContainer();
		if (container != null) {
			container.resume();
		}
	}

	public boolean isFailProcessing() {
		return failProcessing;
	}

	public void enableFailProcessing() {
		failProcessing = true;
	}

	public void restoreProcessing() {
		failProcessing = false;
	}

	private MessageListenerContainer transferListenerContainer() {
		return listenerEndpointRegistry.getListenerContainer(TRANSFER_LISTENER_ID);
	}
}
