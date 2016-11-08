package com.onlinebank.model.operation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OperationImpl implements Operation {
    private final BigDecimal amount;
    private final OperationType operationType;
    private final List<String> accountNames;

    OperationImpl(OperationType operationType, BigDecimal amount, Collection<String> accountNames) {
        Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) == 1,
                "Positive operation amount expected, got " + amount);
        Preconditions.checkArgument(accountNames.size() <= 2,
                "Operations for 1 and 2 accounts supported, got " + accountNames.size());
        Preconditions.checkArgument(!accountNames.contains(null));

        this.amount = requireNonNull(amount);
        this.operationType = requireNonNull(operationType);
        this.accountNames = ImmutableList.copyOf(accountNames);
    }

    @Override
    public List<String> getAccountNames() {
        return accountNames;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public OperationType getOperationType() {
        return operationType;
    }

    @Override
    public String toString() {
        return "OperationImpl{" +
                "amount=" + amount +
                ", operationType=" + operationType +
                ", accountNames=" + accountNames +
                '}';
    }
}
