package com.sha.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class LocalStack extends Stack {

    private final Vpc vpc;
    private final Cluster ecsCluster;

    public LocalStack(final App app, final String id, final StackProps props) {
        super(app, id, props);

        this.vpc = createVpc();

        DatabaseInstance db = createDatabase("HotelBookingDBInstance", "hotel_booking_db");
        CfnHealthCheck dbHealthCheck = createDbHealthCheck(db, "HotelBookingDBHealthCheck");
        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

        FargateService authService = createFargateService(
                "AuthService",
                "auth-service",
                List.of(8084),
                db,
                Map.of("JWT_SECRET", "YPFTL5BpekKOHBa7bZU+Z6DhwtK7h8eKWoqgDAYq1Kw=")
        );

        authService.getNode().addDependency(dbHealthCheck);
        authService.getNode().addDependency(db);

        FargateService billingService = createFargateService(
                "BillingService",
                "billing-service",
                List.of(8081, 9091),
                db,
                null
        );

        FargateService analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-service",
                List.of(8082),
                db,
                null
        );

        analyticsService.getNode().addDependency(mskCluster);

        FargateService clientService = createFargateService(
                "ClientService",
                "client-service",
                List.of(8080),
                db,
                Map.of("BILLING_SERVICE_ADDRESS", "host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9091")

        );

        clientService.getNode().addDependency(db);
        clientService.getNode().addDependency(dbHealthCheck);
        clientService.getNode().addDependency(billingService);
        clientService.getNode().addDependency(mskCluster);

        this.createApiGateWayService();
    }

    // * client-service.hotel-booking.local this is how default cloud map namespace works
    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "HotelBookingEcsCluster")
                .vpc(this.vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("hotel-booking.local")
                        .build())
                .build();
    }

    private Vpc createVpc() {
        return Vpc.Builder.create(this, "HotelBookingVpc")
                .vpcName("HotelBookingVpc")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder.create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()
                ))
                .vpc(this.vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY) // for local
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build()
                ).build();
    }

    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "HotelBookingMskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.large")
                        .clientSubnets(this.vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId).toList())
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }

    private FargateService createFargateService(String id, String imageName, List<Integer> ports,
                                                DatabaseInstance db, Map<String, String> additionalEnvVars) {

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "TaskDef")
                .memoryLimitMiB(512)
                .cpu(256)
                .build();

        ContainerDefinitionOptions.Builder containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                .logGroupName("/ecs/" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix(imageName)
                        .build()));

        Map<String, String> evnVars = new HashMap<>();
        evnVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
        if (additionalEnvVars != null) evnVars.putAll(additionalEnvVars);

        if (db != null) {
            evnVars.put("SPRING_DATASOURCE_URL", String.format("jdbc:postgresql://%s:%s/%s",
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName));
            evnVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            evnVars.put("SPRING_DATASOURCE_PASSWORD", Objects.requireNonNull(db.getSecret()).secretValueFromJson("password").toString());
            evnVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            evnVars.put("SPRING_SQL_INIT_MODE", "always");
            evnVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }
        containerDefinitionOptions.environment(evnVars);
        taskDefinition.addContainer(imageName + "Container", containerDefinitionOptions.build());

        return FargateService.Builder.create(this, id + "FargateService")
                .cluster(this.ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    private void createApiGateWayService() {

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "ApiGatewayTaskDef")
                .memoryLimitMiB(512)
                .cpu(256)
                .build();

        ContainerDefinitionOptions containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("api-gateway"))
                .environment(Map.of(
                        "SPRING_PROFILES_ACTIVE", "prod",
                        "AUTH_SERVICE_URL", "http://host.docker.internal:8084"
                ))
                .portMappings(List.of(8083).stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();

        taskDefinition.addContainer("ApiGatewayContainer", containerDefinitionOptions);

        ApplicationLoadBalancedFargateService.Builder.create(this, "ApiGatewayFargateService")
                .cluster(this.ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./infrastructure/cdk.out").build());

        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app, "localStack", props);
        app.synth();
        System.out.println("CDK Local Stack synthesized successfully.");
    }
}