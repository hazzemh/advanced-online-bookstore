package com.example.bookstore.integration.order;

import com.example.bookstore.payment.order.OrderServiceClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderPaymentHttpClient implements OrderPaymentQuery {

    private final OrderServiceClient orderServiceClient;

    public OrderPaymentHttpClient(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @Override
    public OrderPaymentView getMyOrderPaymentView(UUID orderId, String userEmail) {
        return orderServiceClient.getMyOrderPaymentView(orderId, userEmail);
    }
}

