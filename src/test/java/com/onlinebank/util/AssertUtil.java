package com.onlinebank.util;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public final class AssertUtil {

    public static void assertBigDecimalEquals(String message, BigDecimal expected, BigDecimal actual) {
        assertTrue(message + " expected: " + expected + " actual: " + actual,
                expected.compareTo(actual) == 0);
    }
}
