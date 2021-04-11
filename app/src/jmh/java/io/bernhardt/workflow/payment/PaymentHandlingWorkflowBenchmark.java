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
        PaymentHandlingWorkflow workflow = setup.client.newWorkflowStub(PaymentHandlingWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
        return workflow.handlePayment(new OrderId(UUID.randomUUID().toString()), setup.amountToSpend, setup.merchantId, setup.userId);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    // FIXME this is a stupid benchmark because we only have one worker. so we're measuring single-worker throughput for two workflows (the main one and the credit card one), which is rather meaningless
    public PaymentResult runWorkflowThroughput(WorkflowSetup setup) {
        PaymentHandlingWorkflow workflow = setup.client.newWorkflowStub(PaymentHandlingWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
        return workflow.handlePayment(new OrderId(UUID.randomUUID().toString()), setup.amountToSpend, setup.merchantId, setup.userId);
    }

}
