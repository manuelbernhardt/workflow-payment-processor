package io.bernhardt.workflow.payment;

import io.temporal.client.WorkflowOptions;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PaymentHandlingWorkflowBenchmark {

    @Benchmark
    @BenchmarkMode({Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 1)
    @Measurement(iterations = 10, time = 10)
    @Fork(value = 1, warmups = 0)
    public PaymentResult runWorkflowAvgExecutionTime(WorkflowSetup setup) {
        PaymentHandlingWorkflow workflow = setup.client.newWorkflowStub(PaymentHandlingWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(Shared.COMMON_TASK_QUEUE).build());
        return workflow.handlePayment(new OrderId(UUID.randomUUID().toString()), setup.amountToSpend, setup.merchantId, setup.userId);
    }

}
