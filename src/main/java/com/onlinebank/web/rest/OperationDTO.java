package com.onlinebank.web.rest;

import com.onlinebank.model.operation.Operation;
import com.onlinebank.model.operation.OperationType;

import java.math.BigDecimal;
import java.util.List;

public class OperationDTO implements Operation {

    private List<String> accountNames;
    private BigDecimal amount;
    private OperationType operationType;

    public OperationDTO() {
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
}
