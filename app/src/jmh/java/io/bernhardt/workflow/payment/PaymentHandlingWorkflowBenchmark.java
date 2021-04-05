package io.bernhardt.workflow.payment;

import io.temporal.client.WorkflowOptions;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.bernhardt.workflow.payment.WorkflowSetup.TASK_QUEUE;

public class PaymentHandlingWorkflowBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public PaymentResult runWorkflowAvgExecutionTime(WorkflowSetup setup) {
        // Start a workflow execution. Usually this is done from another program.
        // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.
        PaymentHandlingWorkflow workflow = setup.client.newWorkflowStub(PaymentHandlingWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
        // Execute a workflow waiting for it to complete.
        return workflow.handlePayment(new OrderId(UUID.randomUUID().toString()), setup.amountToSpend, setup.merchantId, setup.userId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public PaymentResult runWorkflowThroughput(WorkflowSetup setup) {
        // Start a workflow execution. Usually this is done from another program.
        // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.
        PaymentHandlingWorkflow workflow = setup.client.newWorkflowStub(PaymentHandlingWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
        // Execute a workflow waiting for it to complete.
        return workflow.handlePayment(new OrderId(UUID.randomUUID().toString()), setup.amountToSpend, setup.merchantId, setup.userId);
    }

}
