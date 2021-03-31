package io.bernhardt.workflow.payment

import io.bernhardt.workflow.payment.converter.ExtendedConverter
import io.bernhardt.workflow.payment.creditcard.CreditCardDetails
import io.bernhardt.workflow.payment.creditcard.IssuerCardId
import io.bernhardt.workflow.payment.creditcard.impl.CreditCardProcessingActivityImpl
import io.bernhardt.workflow.payment.creditcard.impl.CreditCardProcessingWorkflowImpl
import io.bernhardt.workflow.payment.creditcard.impl.MemoryCreditCardStorage
import io.bernhardt.workflow.payment.impl.MemoryConfigurationServiceImpl
import io.bernhardt.workflow.payment.impl.PaymentHandlingActivityImpl
import io.bernhardt.workflow.payment.impl.PaymentHandlingWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.client.WorkflowOptions
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import javax.money.Monetary
import kotlin.system.exitProcess

fun main() {

    val TASK_QUEUE = "PaymentHandling"

    // force using our extended converter
    DefaultDataConverter.setDefaultDataConverter(ExtendedConverter())

    // mock data
    val merchantId = MerchantId("merchantA")
    val userId = UserId("john")
    val creditCardId = CreditCardId("42")
    val configurationService = MemoryConfigurationServiceImpl()
    configurationService.storeMerchantConfiguration(merchantId, MerchantConfiguration(merchantId, BankIdentifier("bankA")))
    configurationService.storeUserConfiguration(userId, UserConfiguration(CreditCard(creditCardId)))
    val creditCardStorage = MemoryCreditCardStorage()
    creditCardStorage.storeCreditCard(creditCardId, CreditCardDetails(creditCardId, userId, "1234", BankIdentifier("bankB"), IssuerCardId("foo")))

    // gRPC stubs wrapper that talks to the local docker instance of temporal service.
    val service = WorkflowServiceStubs.newInstance()

    val clientOpts = WorkflowClientOptions.newBuilder()
            .setDataConverter(ExtendedConverter())
            .build()

    // client that can be used to start and signal workflows
    val client = WorkflowClient.newInstance(service, clientOpts)

    // worker factory that can be used to create workers for specific task queues
    val factory = WorkerFactory.newInstance(client)

    // Worker that listens on a task queue and hosts both workflow and activity implementations.
    val worker = factory.newWorker(TASK_QUEUE)
    worker.registerWorkflowImplementationTypes(PaymentHandlingWorkflowImpl::class.java, CreditCardProcessingWorkflowImpl::class.java)
    worker.registerActivitiesImplementations(PaymentHandlingActivityImpl(configurationService), CreditCardProcessingActivityImpl(creditCardStorage))

    // Start listening to the workflow task queue.
    factory.start()

    // Start a workflow execution. Usually this is done from another program.
    // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.
    val workflow = client.newWorkflowStub(PaymentHandlingWorkflow::class.java, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build())

    // Execute a workflow waiting for it to complete.
    val amount = Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(21).create()
    val paymentResult = workflow.handlePayment(OrderId("helloWorld"), amount, merchantId, userId)

    println(paymentResult)

    exitProcess(0)
}
