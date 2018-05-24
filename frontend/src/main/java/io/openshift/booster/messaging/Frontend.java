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

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.Schedule;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};queue.queue1=responses"),
    })
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Frontend implements MessageListener {
    private List<Response> responses;
    private Map<String, WorkerStatus> workerStatus;

    public Frontend() {
        this.responses = new LinkedList<>();
        this.workerStatus = new HashMap<>();
    }

    public void onMessage(Message message) {
        System.err.println("A message! " + message);
    }

    @ApplicationPath("/api")
    @Path("/")
    public static class RestApi extends Application {
        @GET
        @Path("data")
        @Produces("application/json")
        public String getData() {
            return "\"Data!\"";
        }

        @POST
        @Path("send-request")
        @Consumes("application/x-www-form-urlencoded")
        public void sendRequest(@FormParam("text") String text) {
            System.err.println("XXX " + text);
            
            ConnectionFactory factory = lookupConnectionFactory();

            try {
                Connection conn = factory.createConnection();

                conn.start();

                try {
                    Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Queue requestQueue = session.createQueue("requests");
                    Queue responseQueue = session.createQueue("responses");
                    MessageProducer producer = session.createProducer(requestQueue);
                    MessageConsumer consumer = session.createConsumer(responseQueue);

                    TextMessage message = session.createTextMessage();

                    message.setText(text);
                    message.setJMSReplyTo(responseQueue);

                    producer.send(message);
                } finally {
                    conn.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static ConnectionFactory lookupConnectionFactory() {
        try {
            InitialContext context = new InitialContext();

            try {
                return (ConnectionFactory) context.lookup("java:global/jms/default");
            } finally {
                context.close();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
