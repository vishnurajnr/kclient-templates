package leal.abraham.clientsExamples;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.BasicConfigurator;

import java.util.Properties;

public class commonProducer {

    private static final String TOPIC = "TestingTopic";

    public static Properties getConfig (){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "TestingProducer");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // Properties for auth-enabled cluster, SASL PLAIN

        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"test\" password=\"test123\";");
        props.put("sasl.mechanism", "PLAIN");
        props.put("security.protocol", "SASL_PLAINTEXT");

        return props;
    }

    public static void main(final String[] args) {
        BasicConfigurator.configure();

        //Start producer with configurations
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(getConfig());

        try {

            // Start a finite loop, you normally will want this to be a break-bound while loop
            // However, this is a testing loop
            for (long i = 0; i < 3000; i++) {
                //Std generation of fake key and value
                final String orderId = Long.toString(i);
                final String payment = Integer.toString(orderId.hashCode());

                // Generating record without header
                final ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, orderId, payment);

                //Sending records and displaying metadata with a non-blocking callback
                //This allows to log/action on callbacks without a synchronous request
                producer.send(record, ((recordMetadata, e) -> {
                    System.out.println("Record was sent to topic " +
                            recordMetadata.topic() + " with offset " + recordMetadata.offset() + " in partition " + recordMetadata.partition());
                }));
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        //Tell producer to flush before exiting
        producer.flush();
        System.out.printf("Successfully produced messages to a topic called %s%n", TOPIC);

        //Shutdown hook to assure producer close
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));

    }

}
