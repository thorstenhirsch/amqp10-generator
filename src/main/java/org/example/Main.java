package org.example;

import jakarta.jms.*;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;

import javax.naming.Context;
import javax.naming.InitialContext;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            // The configuration for the Qpid InitialContextFactory has been supplied in
            // a jndi.properties file in the classpath, which results in it being picked
            // up automatically by the InitialContext constructor.
            Context context = new InitialContext();

            ConnectionFactory factory = new JmsConnectionFactory("amqp://localhost:5672");
            Destination queue = new JmsQueue("queue");

            Connection connection = factory.createConnection("artemis", "artemis"); // user, password
            connection.setExceptionListener(new MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("session created");

            //MessageConsumer messageConsumer = session.createConsumer(responseQueue);
            MessageProducer messageProducer = session.createProducer(queue);

            //Send some requests and receive the responses.
            String[] requests = new String[] {
                    "Twas brillig, and the slithy toves",
                    "Did gire and gymble in the wabe.",
                    "All mimsy were the borogroves,",
                    "And the mome raths outgrabe."
            };

            for (String request : requests) {
                TextMessage requestMessage = session.createTextMessage(request);
                //requestMessage.setJMSReplyTo(responseQueue);

                messageProducer.send(requestMessage, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                System.out.println("message sent");

//                TextMessage responseMessage = (TextMessage) messageConsumer.receive(2000);
//                if (responseMessage != null) {
//                    System.out.println("[CLIENT] " + request + " ---> " + responseMessage.getText());
//                } else {
//                    System.out.println("[CLIENT] Response for '" + request +"' was not received within the timeout, exiting.");
//                    break;
//                }
            }

            connection.close();
        } catch (Exception exp) {
            System.out.println("[CLIENT] Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            System.out.println("[CLIENT] Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }
}