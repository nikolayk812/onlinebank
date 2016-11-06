package com.onlinebank.model.operation;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO:
 *
 * NonNull
 */
public interface Operation {

    OperationType getOperationType();

    List<String> getAccountNames();

    BigDecimal getAmount();

}
