package jms.assignment1.reservation.system;

import jms.assignment1.reservation.system.listeners.ResponseAsyncListener;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;

public class Reservation {
    public static void main(String[] args) {

        InitialContext initialContext = null;
        Connection connection = null;
        try {
            initialContext = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            JMSContext jmsContext = cf.createContext();
            Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
            consumer.setMessageListener(new ResponseAsyncListener(cf));
            jmsContext.start();

            System.out.println("Listening...");
            System.in.read();

        } catch (NamingException e) {
            throw new RuntimeException("Naming Exception occurred",e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                }
            }

            if(connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }


        }

    }
}
