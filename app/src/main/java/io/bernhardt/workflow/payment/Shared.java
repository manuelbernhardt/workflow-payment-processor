package io.bernhardt.workflow.payment;

public class Shared {

    public static String COMMON_TASK_QUEUE = "PaymentHandling";
    public static String PAYMENT_TASK_QUEUE = "PaymentActivities";
    public static String CC_TASK_QUEUE = "CreditCardActivities";

    public static boolean useDedicatedQueues = true;
}
