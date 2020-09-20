package com.example.consumer.elasticsearch;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class KafkaConsumerElasticSearch {
    public static final Logger logger = LoggerFactory.getLogger(KafkaConsumerElasticSearch.class.getName());

    public static final String ES_HOSTNAME = "localhost";
    public static final int ES_PORT = 9200;
    public static final String ES_SCHEME = "http";
    public static final String NASA_ES_INDEX = "nasa";
    public static final String NASA_ES_INDEX_TYPE = "_doc";

    public static void main(String[] args) throws IOException {
        // Initialize kafka consumer client
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "elasticsearch_group");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList("nasa_topic"));

        // Initialize ElasticSearch client
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(ES_HOSTNAME, ES_PORT, ES_SCHEME)));


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord record : records) {
                String message = record.value().toString();
                logger.info("Indexing message: " + message);

                // To make the consumer idempotent
                // We could create a generic ID using kafka data like "topic" + "partition" + "offset"
                String id = extractIdFromMessage(message);

                IndexRequest indexRequest = new IndexRequest(NASA_ES_INDEX, NASA_ES_INDEX_TYPE, id)
                        .source(message, XContentType.JSON);
                client.index(indexRequest, RequestOptions.DEFAULT);
            }
        }

    }

    private static String extractIdFromMessage(String message) {
        return JsonParser.parseString(message).getAsJsonObject().get("date").getAsString();
    }

}
