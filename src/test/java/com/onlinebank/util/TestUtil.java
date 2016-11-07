package com.onlinebank.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public final class TestUtil {
    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);
    private static final double DEFAULT_PRECISION = 0.00000001;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void assertBigDecimalEquals(String message, BigDecimal expected, BigDecimal actual) {
        assertTrue(message + " expected: " + expected + " actual: " + actual,
                expected.compareTo(actual) == 0);
    }

    public static ResultActions print(ResultActions action) throws UnsupportedEncodingException {
        log.info("Response content: {}", getContent(action));
        return action;
    }

    private static String getContent(ResultActions action) throws UnsupportedEncodingException {
        return action.andReturn().getResponse().getContentAsString();
    }

    public static Matcher<Double> closeTo(double value) {
        return org.hamcrest.Matchers.closeTo(value, DEFAULT_PRECISION);
    }

    public static byte[] toJson(Object object) throws IOException {
        return MAPPER.writeValueAsBytes(object);
    }
}
