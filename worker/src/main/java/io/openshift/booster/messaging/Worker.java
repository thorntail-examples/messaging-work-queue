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
import javax.ejb.Singleton;
import javax.ejb.Schedule;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Worker {
    static String id = "swarm-" + (Math.round(Math.random() * (10000 - 1000)) + 1000);
    static AtomicInteger requestsProcessed = new AtomicInteger(0);

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void sendStatusUpdate() {
        System.out.println("WORKER-SWARM: Sending status update");

        ConnectionFactory factory = lookupConnectionFactory();

        try {
            Connection conn = factory.createConnection();

            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic("worker-status");
                MessageProducer producer = session.createProducer(topic);

                Message message = session.createTextMessage();
                message.setStringProperty("worker_id", id);
                message.setLongProperty("timestamp", System.currentTimeMillis());
                message.setLongProperty("requests_processed", requestsProcessed.get());

                producer.send(message);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    static ConnectionFactory lookupConnectionFactory() {
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
