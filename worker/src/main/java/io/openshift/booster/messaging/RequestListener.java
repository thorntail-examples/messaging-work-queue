/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster.messaging;

import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};queue.queue1=requests"),
    })
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RequestListener implements MessageListener {
    @Inject
    private Worker worker;
    
    public void onMessage(Message message) {
        TextMessage request = (TextMessage) message;

        try {
            System.out.println("WORKER-SWARM: Received request '" + request.getText() + "'");
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        String responseText;

        try {
            responseText = processRequest(request);
        } catch (Exception e) {
            System.err.println("WORKER-SWARM: Failed processing message: " + e);
            return;
        }

        System.out.println("WORKER-SWARM: Sending response '" + responseText + "'");

        ConnectionFactory factory = worker.lookupConnectionFactory();

        try {
            Connection conn = factory.createConnection();

            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(null);

                TextMessage response = session.createTextMessage(responseText);
                response.setJMSCorrelationID(request.getJMSMessageID());
                response.setStringProperty("worker_id", worker.id);

                producer.send(request.getJMSReplyTo(), response);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        worker.requestsProcessed.incrementAndGet();
    }

    private String processRequest(TextMessage request) throws Exception {
        return request.getText().toUpperCase();
    }
}
