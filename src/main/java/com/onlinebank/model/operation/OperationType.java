package com.onlinebank.model.operation;


import com.google.common.base.Preconditions;

/**
 * Operation type
 */
public enum OperationType {
    DEPOSIT(1),
    WITHDRAWAL(1),
    TRANSFER(2);

    private int accountNumber;

    OperationType(int number) {
        Preconditions.checkArgument(number > 0);
        this.accountNumber = number;
    }

    /**
     * @return required number of accounts for this operation
     */
    public int getAccountNumber() {
        return accountNumber;
    }
}
