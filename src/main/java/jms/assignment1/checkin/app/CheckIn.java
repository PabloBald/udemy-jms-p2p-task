package jms.assignment1.checkin.app;

import jms.assignment1.model.Passenger;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CheckIn {
    public static void main(String[] args) throws IOException, NamingException {
            InitialContext initialContext = null;
            initialContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
            Destination responseQueue = (Destination) initialContext.lookup("queue/responseQueue");

            //Produce messages
            try (JMSContext jmsContext = connectionFactory.createContext()) {
                List<Passenger> passengerList = Arrays.asList(
                        new Passenger("p321", "Carlos", "Bianco", "cbianco@gmail.com", "2238885885"),
                        new Passenger("p322", "Karla", "Gomez", "kgomez@gmail.com", "2238885886"),
                        new Passenger("p323", "Leandro", "Perez", "lperez@gmail.com", "2238885887"),
                        new Passenger("p324", "Anna", "Konda", "aconda@gmail.com", "2238885888"),
                        new Passenger("p325", "Ema", "Guatzon", "eguatzon@gmail.com", "2238885889")
                );

                for(Passenger p : passengerList) {
                    try {
                        JMSProducer producer = jmsContext.createProducer();
                        ObjectMessage message = (ObjectMessage) jmsContext.createObjectMessage();
                        message.setObject(p);
                        message.setJMSReplyTo(responseQueue);

                        System.out.println("JMSReplyTo set to: " + message.getJMSReplyTo());
                        producer.send(requestQueue, message);

                        System.out.println("Sent message to queue");
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            //Consumer
            JMSContext jmsContext = connectionFactory.createContext();

                jmsContext.start();
                JMSConsumer consumer = jmsContext.createConsumer(responseQueue);

                consumer.setMessageListener(responseMessage -> {
                    if (!(responseMessage instanceof MapMessage)) {
                        System.out.println("Invalid message type, expected MapMessage");
                    }

                    MapMessage mapMessage = (MapMessage) responseMessage;
                    Boolean isValid = null;
                    try {
                        isValid = mapMessage.getBoolean("validReservation");

                        System.out.println(String.format("{ Status for message id: %s \nReservation status: %s\n}", mapMessage.getJMSCorrelationID(), isValid));
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                });
        System.in.read();
    }
}
