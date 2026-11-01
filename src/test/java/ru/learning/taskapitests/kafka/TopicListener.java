package ru.learning.taskapitests.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

/**
 * Читает топик, начиная с момента создания слушателя: сообщения, которые уже были в топике, не видны.
 * Группа у каждого слушателя своя и случайная, позиции не сохраняются, поэтому тесты не влияют
 * ни на приложение, ни друг на друга.
 */
public class TopicListener implements AutoCloseable {

    public record Message(String key, String value, long offset) {
    }

    private final String topic;
    private final KafkaConsumer<String, String> consumer;
    private final List<Message> received = new ArrayList<>();

    public TopicListener(String bootstrapServers, String topic) {
        this.topic = topic;

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "tests-" + UUID.randomUUID());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(properties);

        List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                .map(info -> new TopicPartition(topic, info.partition()))
                .toList();
        if (partitions.isEmpty()) {
            consumer.close();
            throw new IllegalStateException("Топик " + topic + " не найден на брокере " + bootstrapServers);
        }
        consumer.assign(partitions);
        consumer.seekToEnd(partitions);
        // seekToEnd откладывается до первого запроса позиции: спрашиваем её сразу, чтобы «конец» зафиксировался сейчас
        partitions.forEach(consumer::position);
    }

    /** Ждёт сообщение, подходящее под условие. Возвращает первое подходящее из полученных. */
    public Message awaitMessage(Predicate<Message> predicate, Duration timeout) {
        try {
            await().atMost(timeout)
                    .pollDelay(Duration.ZERO)
                    .pollInterval(Duration.ofMillis(100))
                    .pollInSameThread()
                    .until(() -> {
                        poll();
                        return received.stream().anyMatch(predicate);
                    });
        } catch (ConditionTimeoutException e) {
            throw new AssertionError("За " + timeout.toSeconds() + " с в топике " + topic
                    + " не появилось нужное сообщение. Получено сообщений: " + received.size()
                    + describeReceived(), e);
        }
        return received.stream().filter(predicate).findFirst().orElseThrow();
    }

    /** Читает топик заданное время (для проверок «сообщения не появилось» и «не пришло дубля»). */
    public List<Message> collectFor(Duration duration) {
        long deadline = System.nanoTime() + duration.toNanos();
        while (System.nanoTime() < deadline) {
            poll();
        }
        return List.copyOf(received);
    }

    public List<Message> messages() {
        return List.copyOf(received);
    }

    private void poll() {
        for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(200))) {
            received.add(new Message(record.key(), record.value(), record.offset()));
        }
    }

    private String describeReceived() {
        if (received.isEmpty()) {
            return "";
        }
        String last = received.stream()
                .skip(Math.max(0, received.size() - 10))
                .map(message -> "\n  " + message.key() + " | " + abbreviate(message.value()))
                .collect(Collectors.joining());
        return ". Последние:" + last;
    }

    private String abbreviate(String value) {
        return value == null || value.length() <= 200 ? value : value.substring(0, 200) + "…";
    }

    @Override
    public void close() {
        consumer.close(Duration.ofSeconds(5));
    }
}