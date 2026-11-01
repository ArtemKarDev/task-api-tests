package ru.learning.taskapitests.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Отправляет строковые сообщения в Kafka и дожидается подтверждения брокера. */
public class KafkaProducerClient implements AutoCloseable {

    private final KafkaProducer<String, String> producer;

    public KafkaProducerClient(String bootstrapServers) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
        this.producer = new KafkaProducer<>(properties);
    }

    public void send(String topic, String key, String value) {
        try {
            producer.send(new ProducerRecord<>(topic, key, value)).get(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Отправка в топик " + topic + " прервана", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Не удалось отправить сообщение в топик " + topic, e);
        }
    }

    @Override
    public void close() {
        producer.close(Duration.ofSeconds(5));
    }
}