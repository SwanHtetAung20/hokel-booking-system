package com.sha.client_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.sha.client_service.config.properties.GrpcProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillingServiceGrpcClient {

    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(GrpcProperties grpcProperties) {
        log.info("Initializing BillingServiceGrpcClient with address: {} and port: {}",
                grpcProperties.getAddress(), grpcProperties.getPort());
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                        grpcProperties.getAddress(), grpcProperties.getPort())
                .usePlaintext()
                .build();
        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String clientId, String name, String email) {
        BillingRequest request = BillingRequest.newBuilder()
                .setClientId(clientId)
                .setName(name)
                .setEmail(email)
                .build();
        BillingResponse response = this.blockingStub.createBillingAccount(request);
        log.info("Received BillingResponse: {}", response);
        return response;
    }

}
