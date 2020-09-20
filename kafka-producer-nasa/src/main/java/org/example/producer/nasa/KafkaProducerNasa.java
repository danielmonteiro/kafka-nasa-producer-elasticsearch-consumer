package org.example.producer.nasa;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

public class KafkaProducerNasa {

    private static final String NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=%s&date=%s";
    private static final String API_KEY = "4f35J3BgMiGPKDiB2UEpKEZiuzjfd0nB1xft8hTl";
    private static final int DAYS = 30;
    private static final String NASA_TOPIC = "nasa_topic";

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        DateTime now = DateTime.now();

        for (int i=0; i<DAYS; i++) {
            now = now.minusDays(1);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
            String date = dtf.print(now);

            System.out.println("Date: " + date);

            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(format(NASA_URL, API_KEY, date)));
            String nasaResponse = request.execute().parseAsString();

            System.out.println(nasaResponse);

            ProducerRecord<String, String> record = new ProducerRecord<>(NASA_TOPIC, nasaResponse.trim());
            producer.send(record);
        }

        producer.close();

    }

}
