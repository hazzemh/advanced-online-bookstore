package com.example.bookstore.payment.order;

import com.example.bookstore.integration.order.OrderPaymentView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    @Value("${order-service.base-url}")
    private String baseUrl;

    @Value("${internal.api.token}")
    private String internalToken;

    public OrderServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OrderPaymentView getMyOrderPaymentView(UUID orderId, String userEmail) {
        return restClient.get()
                .uri(baseUrl + "/internal/orders/{orderId}/payment-view?userEmail={userEmail}", orderId, userEmail)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .body(OrderPaymentView.class);
    }

    public void updateOrderStatus(UUID orderId, String status) {
        restClient.post()
                .uri(baseUrl + "/internal/orders/{orderId}/status", orderId)
                .header("X-Internal-Token", internalToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateInternalOrderStatusRequest(status))
                .retrieve()
                .toBodilessEntity();
    }
}

