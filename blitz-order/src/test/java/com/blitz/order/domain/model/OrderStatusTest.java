package com.blitz.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OrderStatus} enum — state machine transition rules.
 */
class OrderStatusTest {

    // ---- canTransitionTo positive cases ----

    @Test
    @DisplayName("PENDING_PAYMENT canTransitionTo PAID and CANCELLED")
    void pendingPaymentCanTransitionToPaidAndCancelled() {
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PAID),
            "PENDING_PAYMENT should be able to transition to PAID");
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.CANCELLED),
            "PENDING_PAYMENT should be able to transition to CANCELLED");
    }

    @Test
    @DisplayName("PAID canTransitionTo SHIPPED and REFUNDED")
    void paidCanTransitionToShippedAndRefunded() {
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED),
            "PAID should be able to transition to SHIPPED");
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.REFUNDED),
            "PAID should be able to transition to REFUNDED");
    }

    @Test
    @DisplayName("SHIPPED canTransitionTo RECEIVED only")
    void shippedCanTransitionToReceivedOnly() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.RECEIVED),
            "SHIPPED should be able to transition to RECEIVED");

        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PENDING_PAYMENT));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PAID));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.REFUNDED));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.SHIPPED));
    }

    // ---- canTransitionTo negative cases ----

    @Test
    @DisplayName("PENDING_PAYMENT cannot transition to SHIPPED, RECEIVED, REFUNDED")
    void pendingPaymentCannotTransitionToShippedReceivedRefunded() {
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED),
            "PENDING_PAYMENT should NOT transition to SHIPPED");
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.RECEIVED),
            "PENDING_PAYMENT should NOT transition to RECEIVED");
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.REFUNDED),
            "PENDING_PAYMENT should NOT transition to REFUNDED");
    }

    @Test
    @DisplayName("RECEIVED/CANCELLED/REFUNDED are terminal (no valid transitions)")
    void terminalStatesHaveNoValidTransitions() {
        for (OrderStatus terminal : new OrderStatus[]{
            OrderStatus.RECEIVED, OrderStatus.CANCELLED, OrderStatus.REFUNDED
        }) {
            for (OrderStatus target : OrderStatus.values()) {
                assertFalse(terminal.canTransitionTo(target),
                    terminal + " should NOT transition to " + target);
            }
        }
    }

    // ---- validateTransition ----

    @Test
    @DisplayName("validateTransition should NOT throw for valid transitions")
    void validateTransitionDoesNotThrowForValidTransitions() {
        assertDoesNotThrow(() -> OrderStatus.PENDING_PAYMENT.validateTransition(OrderStatus.PAID));
        assertDoesNotThrow(() -> OrderStatus.PAID.validateTransition(OrderStatus.SHIPPED));
        assertDoesNotThrow(() -> OrderStatus.SHIPPED.validateTransition(OrderStatus.RECEIVED));
    }

    @Test
    @DisplayName("validateTransition throws IllegalStateException on invalid transition")
    void validateTransitionThrowsOnInvalidTransition() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> OrderStatus.PENDING_PAYMENT.validateTransition(OrderStatus.SHIPPED));
        assertTrue(ex.getMessage().contains("Invalid order state transition"),
            "Exception message should mention invalid transition");
    }

    @Test
    @DisplayName("validateTransition throws for self-transition")
    void validateTransitionThrowsForSelfTransition() {
        assertThrows(IllegalStateException.class,
            () -> OrderStatus.PAID.validateTransition(OrderStatus.PAID));
    }

    // ---- fromCode ----

    @Test
    @DisplayName("fromCode(0-5) returns correct enum")
    void fromCodeReturnsCorrectEnum() {
        assertEquals(OrderStatus.PENDING_PAYMENT, OrderStatus.fromCode(0));
        assertEquals(OrderStatus.PAID, OrderStatus.fromCode(1));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromCode(2));
        assertEquals(OrderStatus.RECEIVED, OrderStatus.fromCode(3));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromCode(4));
        assertEquals(OrderStatus.REFUNDED, OrderStatus.fromCode(5));
    }

    @Test
    @DisplayName("fromCode(99) throws IllegalArgumentException")
    void fromCodeWithInvalidCodeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderStatus.fromCode(99));
    }

    @Test
    @DisplayName("fromCode(-1) throws IllegalArgumentException")
    void fromCodeWithNegativeCodeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderStatus.fromCode(-1));
    }

    // ---- code consistency ----

    @Test
    @DisplayName("fromCode round-trip through getCode returns same enum")
    void fromCodeRoundTrip() {
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(status, OrderStatus.fromCode(status.getCode()),
                "Round-trip failed for " + status);
        }
    }
}
