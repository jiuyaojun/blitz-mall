package com.blitz.order.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Order} entity — behavioral methods and state transitions.
 */
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setOrderNo("SN20240101000001");
        order.setUserId(100L);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTotalAmount(new BigDecimal("299.00"));
        order.setFreightAmount(new BigDecimal("10.00"));
        order.setDiscountAmount(new BigDecimal("9.00"));
    }

    // ---- pay() ----

    @Test
    @DisplayName("pay() sets status to PAID, records paymentTime and paymentMethod")
    void paySetsPaidStatusAndPaymentInfo() {
        order.pay(2);

        assertEquals(OrderStatus.PAID.getCode(), order.getStatus(),
            "Status should be PAID after pay()");
        assertEquals(2, order.getPaymentMethod(),
            "Payment method should be recorded");
        assertNotNull(order.getPaymentTime(),
            "Payment time should be set");
        assertTrue(order.getPaymentTime().isBefore(LocalDateTime.now().plusSeconds(1)),
            "Payment time should be now or recent");
    }

    @Test
    @DisplayName("pay() on already PAID order throws IllegalStateException")
    void payOnAlreadyPaidOrderThrows() {
        order.setStatus(OrderStatus.PAID.getCode());

        assertThrows(IllegalStateException.class, () -> order.pay(1),
            "Paying an already PAID order should throw");
    }

    @Test
    @DisplayName("pay() on CANCELLED order throws IllegalStateException")
    void payOnCancelledOrderThrows() {
        order.setStatus(OrderStatus.CANCELLED.getCode());

        assertThrows(IllegalStateException.class, () -> order.pay(1),
            "Paying a CANCELLED order should throw");
    }

    // ---- ship() ----

    @Test
    @DisplayName("ship() transitions PAID to SHIPPED")
    void shipTransitionsPaidToShipped() {
        order.setStatus(OrderStatus.PAID.getCode());

        order.ship();

        assertEquals(OrderStatus.SHIPPED.getCode(), order.getStatus(),
            "Status should be SHIPPED after ship()");
        assertNotNull(order.getDeliveryTime(),
            "Delivery time should be set");
    }

    @Test
    @DisplayName("ship() on PENDING_PAYMENT order throws IllegalStateException")
    void shipOnPendingPaymentThrows() {
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        assertThrows(IllegalStateException.class, () -> order.ship(),
            "Shipping a PENDING_PAYMENT order should throw");
    }

    // ---- receive() ----

    @Test
    @DisplayName("receive() transitions SHIPPED to RECEIVED")
    void receiveTransitionsShippedToReceived() {
        order.setStatus(OrderStatus.SHIPPED.getCode());

        order.receive();

        assertEquals(OrderStatus.RECEIVED.getCode(), order.getStatus(),
            "Status should be RECEIVED after receive()");
        assertNotNull(order.getReceiveTime(),
            "Receive time should be set");
    }

    @Test
    @DisplayName("receive() on PENDING_PAYMENT order throws IllegalStateException")
    void receiveOnPendingPaymentThrows() {
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        assertThrows(IllegalStateException.class, () -> order.receive(),
            "Receiving a PENDING_PAYMENT order should throw");
    }

    // ---- cancel() ----

    @Test
    @DisplayName("cancel() transitions PENDING_PAYMENT to CANCELLED")
    void cancelTransitionsPendingPaymentToCancelled() {
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        order.cancel();

        assertEquals(OrderStatus.CANCELLED.getCode(), order.getStatus(),
            "Status should be CANCELLED after cancel()");
        assertNotNull(order.getCancelTime(),
            "Cancel time should be set");
    }

    @Test
    @DisplayName("cancel() on PAID order throws IllegalStateException")
    void cancelOnPaidOrderThrows() {
        order.setStatus(OrderStatus.PAID.getCode());

        assertThrows(IllegalStateException.class, () -> order.cancel(),
            "Cancelling a PAID order should throw");
    }

    @Test
    @DisplayName("cancel() on SHIPPED order throws IllegalStateException")
    void cancelOnShippedOrderThrows() {
        order.setStatus(OrderStatus.SHIPPED.getCode());

        assertThrows(IllegalStateException.class, () -> order.cancel(),
            "Cancelling a SHIPPED order should throw");
    }

    // ---- refund() ----

    @Test
    @DisplayName("refund() transitions PAID to REFUNDED")
    void refundTransitionsPaidToRefunded() {
        order.setStatus(OrderStatus.PAID.getCode());

        order.refund();

        assertEquals(OrderStatus.REFUNDED.getCode(), order.getStatus(),
            "Status should be REFUNDED after refund()");
    }

    @Test
    @DisplayName("refund() on PENDING_PAYMENT order throws IllegalStateException")
    void refundOnPendingPaymentThrows() {
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        assertThrows(IllegalStateException.class, () -> order.refund(),
            "Refunding a PENDING_PAYMENT order should throw");
    }

    // ---- canCancel() ----

    @Test
    @DisplayName("canCancel() returns true only for PENDING_PAYMENT")
    void canCancelReturnsTrueOnlyForPendingPayment() {
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        assertTrue(order.canCancel(),
            "canCancel should be true for PENDING_PAYMENT");

        order.setStatus(OrderStatus.PAID.getCode());
        assertFalse(order.canCancel(),
            "canCancel should be false for PAID");

        order.setStatus(OrderStatus.SHIPPED.getCode());
        assertFalse(order.canCancel(),
            "canCancel should be false for SHIPPED");

        order.setStatus(OrderStatus.RECEIVED.getCode());
        assertFalse(order.canCancel(),
            "canCancel should be false for RECEIVED");

        order.setStatus(OrderStatus.CANCELLED.getCode());
        assertFalse(order.canCancel(),
            "canCancel should be false for CANCELLED");

        order.setStatus(OrderStatus.REFUNDED.getCode());
        assertFalse(order.canCancel(),
            "canCancel should be false for REFUNDED");
    }

    // ---- getPayableAmount() ----

    @Test
    @DisplayName("getPayableAmount() = total + freight - discount")
    void getPayableAmountCorrectFormula() {
        BigDecimal payable = order.getPayableAmount();

        assertEquals(0, new BigDecimal("300.00").compareTo(payable),
            "Payable should be 299 + 10 - 9 = 300.00, but was " + payable);
    }

    @Test
    @DisplayName("getPayableAmount() with null amounts defaults to zero")
    void getPayableAmountWithNullAmounts() {
        order.setTotalAmount(null);
        order.setFreightAmount(null);
        order.setDiscountAmount(null);

        BigDecimal payable = order.getPayableAmount();

        assertEquals(0, BigDecimal.ZERO.compareTo(payable),
            "Payable with all null should be ZERO");
    }

    @Test
    @DisplayName("getPayableAmount() with null freight and discount")
    void getPayableAmountWithPartialNulls() {
        order.setFreightAmount(null);
        order.setDiscountAmount(null);

        BigDecimal payable = order.getPayableAmount();

        assertEquals(0, new BigDecimal("299.00").compareTo(payable),
            "Payable with null freight/discount should be total only");
    }

    // ---- getOrderStatus() ----

    @Test
    @DisplayName("getOrderStatus() returns correct OrderStatus from status code")
    void getOrderStatusReturnsCorrectEnum() {
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());

        order.setStatus(OrderStatus.PAID.getCode());
        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        order.setStatus(OrderStatus.SHIPPED.getCode());
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());

        order.setStatus(OrderStatus.RECEIVED.getCode());
        assertEquals(OrderStatus.RECEIVED, order.getOrderStatus());

        order.setStatus(OrderStatus.CANCELLED.getCode());
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());

        order.setStatus(OrderStatus.REFUNDED.getCode());
        assertEquals(OrderStatus.REFUNDED, order.getOrderStatus());
    }
}
