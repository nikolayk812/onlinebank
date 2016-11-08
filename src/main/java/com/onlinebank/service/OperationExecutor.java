package com.onlinebank.service;

import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;
import com.onlinebank.model.operation.OperationType;
import com.onlinebank.repo.AccountRepository;
import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * Operation executor.
 * Applies operation to a single account or multiple accounts.
 * Business logic is concentrated in this class.
 *
 * Locks should be already taken in {@link AccountService}
 */
@Component
public class OperationExecutor {
    private final static BiFunction<Account, Operation, BigDecimal> ADD_FUNCTION =
            ((account, operation) -> account.getBalance().add(operation.getAmount()));
    private final static BiFunction<Account, Operation, BigDecimal> SUBTRACT_FUNCTION =
            ((account, operation) -> account.getBalance().subtract(operation.getAmount()));


    @Autowired
    private AccountRepository repo;

    /**
     * Execute operation at account(s)
     * Should run already in transaction context
     *
     * @param operation account operation
     * @return source account of operation
     * @throws OperationException in case operation fails for any reason
     */
    public Account execute(Operation operation) throws OperationException {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new OperationException(operation, "No active transaction");

        OperationType type = operation.getOperationType();
        try {
            switch (type) {
                case DEPOSIT:
                    return deposit(operation);
                case WITHDRAWAL:
                    return withdraw(operation);
                case TRANSFER:
                    return transfer(operation);
                default:
                    throw new IllegalStateException("Wrong operation type: " + type);
            }
        } catch (Exception e) {
            throw new OperationException(operation, "Operation failed", e);
        }
    }

    private Account transfer(Operation operation) {
        assert operation.getOperationType() == OperationType.TRANSFER;

        BigDecimal amount = operation.getAmount();
        String fromAccountName = operation.getAccountNames().get(0);
        Account fromAccount = repo.findOneByName(fromAccountName)
                .orElseThrow(() -> new OperationException(operation, "Account '" + fromAccountName + "' does not exist"));

        String toAccountName = operation.getAccountNames().get(1);
        Account toAccount = repo.findOneByName(toAccountName)
                .orElseThrow(() -> new OperationException(operation, "Account '" + toAccountName + "' does not exist"));

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        repo.save(toAccount);
        return repo.save(fromAccount);
    }

    private Account deposit(Operation operation) {
        assert operation.getOperationType() == OperationType.DEPOSIT;
        return executeSingleAccountOperation(operation, ADD_FUNCTION);
    }

    private Account withdraw(Operation operation) {
        assert operation.getOperationType() == OperationType.WITHDRAWAL;
        return executeSingleAccountOperation(operation, SUBTRACT_FUNCTION);
    }

    private Account executeSingleAccountOperation(Operation operation,
                                                  BiFunction<Account, Operation, BigDecimal> biFunction) {
        String name = operation.getAccountNames().get(0);
        return repo.findOneByName(name)
                .map(account -> {
                    BigDecimal newAmount = biFunction.apply(account, operation);
                    account.setBalance(newAmount);
                    return repo.save(account);
                })
                .orElseThrow(() -> new NotFoundException(name));
    }

}
