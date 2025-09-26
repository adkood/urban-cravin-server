//package com.ashutosh.urban_cravin.configs;
//
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    public static final String ORDER_EXCHANGE = "order.exchange";
//
//    // Stock queue
//    public static final String STOCK_QUEUE = "order.stock.queue";
//    public static final String STOCK_RETRY_QUEUE = "order.stock.retry.queue";
//    public static final String STOCK_DLQ = "order.stock.dlq";
//    public static final String STOCK_ROUTING = "order.placed.stock";
//
//    // Exchange
//    @Bean
//    public DirectExchange orderExchange() {
//        return new DirectExchange(ORDER_EXCHANGE);
//    }
//
//    @Bean
//    public Queue stockQueue() {
//        return QueueBuilder.durable(STOCK_QUEUE)
//                .withArgument("x-dead-letter-exchange", "")
//                .withArgument("x-dead-letter-routing-key", STOCK_RETRY_QUEUE)
//                .build();
//    }
//
//    @Bean
//    public Queue stockRetryQueue() {
//        return QueueBuilder.durable(STOCK_RETRY_QUEUE)
//                .withArgument("x-dead-letter-exchange", "")
//                .withArgument("x-dead-letter-routing-key", STOCK_QUEUE)
//                .withArgument("x-message-ttl", 30_000)
//                .build();
//    }
//
//    @Bean
//    public Queue stockDlq() {
//        return QueueBuilder.durable(STOCK_DLQ).build();
//    }
//
//    @Bean
//    public Binding bindingStock(Queue stockQueue, DirectExchange orderExchange) {
//        return BindingBuilder.bind(stockQueue).to(orderExchange).with(STOCK_ROUTING);
//    }
//
//    @Bean
//    public Binding bindingStockRetry(Queue stockRetryQueue, DirectExchange orderExchange) {
//        return BindingBuilder.bind(stockRetryQueue).to(orderExchange).with(STOCK_RETRY_QUEUE);
//    }
//
//    @Bean
//    public Binding bindingStockDlq(Queue stockDlq, DirectExchange orderExchange) {
//        return BindingBuilder.bind(stockDlq).to(orderExchange).with(STOCK_DLQ);
//    }
//
//}
