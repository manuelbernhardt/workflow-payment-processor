package io.bernhardt.workflow.payment;

import io.bernhardt.workflow.payment.converter.ExtendedConverter;
import io.bernhardt.workflow.payment.creditcard.CreditCardDetails;
import io.bernhardt.workflow.payment.creditcard.CreditCardStorage;
import io.bernhardt.workflow.payment.creditcard.IssuerBankClient;
import io.bernhardt.workflow.payment.creditcard.IssuerCardId;
import io.bernhardt.workflow.payment.creditcard.impl.CreditCardProcessingActivitiesImpl;
import io.bernhardt.workflow.payment.creditcard.impl.MemoryCreditCardStorage;
import io.bernhardt.workflow.payment.creditcard.impl.RandomLatencyIssuerBankClient;
import io.bernhardt.workflow.payment.impl.MemoryConfigurationServiceImpl;
import io.bernhardt.workflow.payment.impl.PaymentHandlingActivitiesImpl;
import io.bernhardt.workflow.payment.impl.PaymentHandlingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.lang.reflect.Method;
import java.time.Duration;

@State(Scope.Benchmark)
public class WorkflowSetup {

    protected boolean useDedicatedTaskQueues = Shared.useDedicatedQueues;

    MerchantId merchantId = new MerchantId("merchantA");
    UserId userId = new UserId("john");
    CreditCardId creditCardId = new CreditCardId("42");

    Integer amountToSpend = 21;

    WorkerFactory factory = null;
    Worker worker = null;
    Worker worker2 = null;
    WorkflowClient client = null;

    @Setup(Level.Trial)
    public void setupWorkflowClient() {
        // force using our extended converter
        DefaultDataConverter.setDefaultDataConverter(new ExtendedConverter());

        // mock data
        ConfigurationService configurationService = new MemoryConfigurationServiceImpl();
        configurationService.storeMerchantConfiguration(merchantId, new MerchantConfiguration(merchantId, new BankIdentifier("bankA")));
        configurationService.storeUserConfiguration(userId, new UserConfiguration(new CreditCard(creditCardId)));
        CreditCardStorage creditCardStorage = new MemoryCreditCardStorage();
        int spendingLimit = Integer.MAX_VALUE;
        IssuerBankClient issuerBankClient = new RandomLatencyIssuerBankClient(spendingLimit, Duration.ofMillis(0), Duration.ofMillis(0));

        creditCardStorage.storeCreditCard(creditCardId, new CreditCardDetails(creditCardId, userId, "1234", new BankIdentifier("bankB"), new IssuerCardId("foo")));

        // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();

        WorkflowClientOptions clientOpts = WorkflowClientOptions.newBuilder()
                .setDataConverter(new ExtendedConverter())
                .build();

        // client that can be used to start and signal workflows
        WorkflowClient client = WorkflowClient.newInstance(service, clientOpts);

        WorkerFactoryOptions factoryOptions = WorkerFactoryOptions.newBuilder().setWorkflowHostLocalPollThreadCount(5).build();

        // worker factory that can be used to create workers for specific task queues
        this.factory = WorkerFactory.newInstance(client, factoryOptions);

        if (useDedicatedTaskQueues) {
            worker = factory.newWorker(Shared.PAYMENT_TASK_QUEUE);
            worker.registerWorkflowImplementationTypes(PaymentHandlingWorkflowImpl.class);
            worker.registerActivitiesImplementations(new PaymentHandlingActivitiesImpl(configurationService));

            worker2 = factory.newWorker(Shared.CC_TASK_QUEUE);
            worker2.registerWorkflowImplementationTypes(PaymentHandlingWorkflowImpl.class);
            worker.registerActivitiesImplementations(new CreditCardProcessingActivitiesImpl(creditCardStorage, issuerBankClient));
        } else {
            worker = factory.newWorker(Shared.COMMON_TASK_QUEUE);
            worker.registerWorkflowImplementationTypes(PaymentHandlingWorkflowImpl.class);
            worker.registerActivitiesImplementations(new PaymentHandlingActivitiesImpl(configurationService), new CreditCardProcessingActivitiesImpl(creditCardStorage, issuerBankClient));
            worker2 = factory.newWorker(Shared.COMMON_TASK_QUEUE);
            worker2.registerWorkflowImplementationTypes(PaymentHandlingWorkflowImpl.class);
            worker2.registerActivitiesImplementations(new PaymentHandlingActivitiesImpl(configurationService), new CreditCardProcessingActivitiesImpl(creditCardStorage, issuerBankClient));
        }

        // Start listening to the workflow task queue.
        factory.start();

        this.client = client;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        // this doesn't work, something (probably a thread pool) is preventing clean shutdown
        factory.shutdownNow();
        try {
            Method shutdownNow = Worker.class.getDeclaredMethod("shutdownNow");
            shutdownNow.setAccessible(true);
            shutdownNow.invoke(worker);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
