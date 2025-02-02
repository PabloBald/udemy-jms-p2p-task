package jms.assignment1.reservation.system.listeners;


import jms.assignment1.model.Passenger;

import javax.jms.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ResponseAsyncListener implements MessageListener {

    private static AtomicInteger listenerNumber = new AtomicInteger(0);

    private static HashMap<String, Boolean> reservations;

    static {
        reservations = new HashMap<>();
        reservations.put("p321", true);
        reservations.put("p322", false);
        reservations.put("p323", false);
        reservations.put("p324", true);
        reservations.put("p325", false);
    }

    private ConnectionFactory connectionFactory;

    public ResponseAsyncListener(ConnectionFactory cf) {
        connectionFactory = cf;
        listenerNumber.getAndIncrement();
    }

    @Override
    public void onMessage(Message message) {
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            if (message != null) {
                Destination replyTo = null;
                System.out.println(String.format("ResponseAsyncListener %s -received a message", listenerNumber.get()));
                if (!(message instanceof ObjectMessage))
                    System.out.println("Invalid message type. Expected ObjectMessage");

                ObjectMessage passengerMessage = (ObjectMessage) message;
                try {
                    replyTo = passengerMessage.getJMSReplyTo();
                    if (replyTo == null) {
                        System.out.println("ReplyTo header is null.");
                        return;
                    }
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
                Passenger p = null;

                try {
                    p = (Passenger) passengerMessage.getObject();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }

                try {
                    MapMessage mapMessage = jmsContext.createMapMessage();
                    mapMessage.setJMSCorrelationID(p.getId());
                    mapMessage.setBoolean("validReservation", reservations.containsKey(p.getId()));

                    JMSProducer producer = jmsContext.createProducer();
                    producer.send(replyTo, mapMessage);
                    System.out.println(String.format("ResponseAsyncListener %s - sent a map message to queue: %s", listenerNumber.get(), replyTo));
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
