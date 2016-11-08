package com.onlinebank.web.rest;

import com.onlinebank.model.operation.Operation;
import com.onlinebank.model.operation.OperationType;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * DTO object for {@link Operation}
 *
 * Note: public setters and getters, empty constructor are required for Jackson.
 */
class OperationDTO implements Operation {

    private List<String> accountNames;
    private BigDecimal amount;
    private OperationType operationType;

    OperationDTO() {
    }

    OperationDTO(List<String> accountNames, BigDecimal amount, OperationType operationType) {
        this.accountNames = requireNonNull(accountNames);
        this.amount = requireNonNull(amount);
        this.operationType = requireNonNull(operationType);
    }

    @Override
    public List<String> getAccountNames() {
        return accountNames;
    }

    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OperationDTO{");
        sb.append("accountNames=").append(accountNames);
        sb.append(", amount=").append(amount);
        sb.append(", operationType=").append(operationType);
        sb.append('}');
        return sb.toString();
    }
}
