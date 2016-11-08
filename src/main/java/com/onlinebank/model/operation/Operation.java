package com.onlinebank.model.operation;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for all account operations
 *
 * All methods should not return {@code null} values.
 */
public interface Operation {

    /**
     * @return operation type
     */
    OperationType getOperationType();

    /**
     * Return list of account names.
     * List must have 1 or 2 items.
     * Should not contain {@code null} values.
     *
     * @return list of account names,
     */
    List<String> getAccountNames();

    /**
     * @return operation amount, should be always positive
     */
    BigDecimal getAmount();

}
