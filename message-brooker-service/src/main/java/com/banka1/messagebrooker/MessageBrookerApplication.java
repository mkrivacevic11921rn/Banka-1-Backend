package com.banka1.messagebrooker;

import org.apache.activemq.broker.BrokerService;

public class MessageBrookerApplication {

    public static void main(String[] args) throws Exception {
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");
        broker.setPersistent(true);
        broker.setUseJmx(true);
        broker.start();

        broker.waitUntilStopped();
    }

}
