package com.sha.billing_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {


    @Override
    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {

        log.info("Received createBillingAccount request: {}", request.toString());
        // do-business-logic-here
        BillingResponse billingResponse = BillingResponse.newBuilder()
                .setAccountId("generated-account-id-123")
                .setStatus("SUCCESS")
                .build();
       responseObserver.onNext(billingResponse);
        responseObserver.onCompleted();
        log.info("Sent createBillingAccount response: {}", billingResponse.toString());
    }
}
