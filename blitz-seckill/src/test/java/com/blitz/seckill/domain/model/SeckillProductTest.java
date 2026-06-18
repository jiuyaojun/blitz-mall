package com.blitz.seckill.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeckillProduct — stock operations")
class SeckillProductTest {

    @Nested
    @DisplayName("hasStock()")
    class HasStock {

        @Test
        @DisplayName("returns true when stock > 0")
        void returnsTrueWhenStockPositive() {
            SeckillProduct sp = new SeckillProduct();
            sp.setSeckillStock(10);
            assertTrue(sp.hasStock());
        }

        @Test
        @DisplayName("returns false when stock is null")
        void returnsFalseWhenStockNull() {
            SeckillProduct sp = new SeckillProduct();
            assertFalse(sp.hasStock());
        }

        @Test
        @DisplayName("returns false when stock is 0")
        void returnsFalseWhenStockZero() {
            SeckillProduct sp = new SeckillProduct();
            sp.setSeckillStock(0);
            assertFalse(sp.hasStock());
        }
    }

    @Nested
    @DisplayName("deductStock()")
    class DeductStock {

        @Test
        @DisplayName("reduces stock by given qty")
        void reducesStockByQty() {
            SeckillProduct sp = new SeckillProduct();
            sp.setSeckillStock(10);
            sp.deductStock(3);
            assertEquals(7, sp.getSeckillStock());
        }

        @Test
        @DisplayName("throws when qty exceeds stock")
        void throwsWhenQtyExceedsStock() {
            SeckillProduct sp = new SeckillProduct();
            sp.setSeckillStock(5);
            assertThrows(IllegalStateException.class, () -> sp.deductStock(10));
        }

        @Test
        @DisplayName("throws when stock is null")
        void throwsWhenStockIsNull() {
            SeckillProduct sp = new SeckillProduct();
            assertThrows(IllegalStateException.class, () -> sp.deductStock(1));
        }
    }

    @Nested
    @DisplayName("restoreStock()")
    class RestoreStock {

        @Test
        @DisplayName("increases stock by given qty")
        void increasesStockByQty() {
            SeckillProduct sp = new SeckillProduct();
            sp.setSeckillStock(5);
            sp.restoreStock(3);
            assertEquals(8, sp.getSeckillStock());
        }

        @Test
        @DisplayName("handles null stock — initializes to 0 then adds")
        void handlesNullStock() {
            SeckillProduct sp = new SeckillProduct();
            sp.restoreStock(5);
            assertEquals(5, sp.getSeckillStock());
        }
    }
}
