package com.onlinebank.service;

import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;
import com.onlinebank.model.operation.OperationType;
import com.onlinebank.repo.AccountRepository;
import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * TODO:
 * <p>
 * Locks should be already taken in {@link AccountServiceImpl}
 * Operation should be validated
 */
@Component
public class OperationExecutor {
    @Autowired
    private AccountRepository repo;

    public Account execute(Operation operation) throws OperationException {
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
            throw new OperationException("Operation " + type + " " +
                    operation.getAmount().toPlainString() + " for account(s) '" +
                    operation.getAccountNames()+ "' failed", e);
        }
    }

    private Account transfer(Operation operation) {
        assert operation.getOperationType() == OperationType.TRANSFER;

        BigDecimal amount = operation.getAmount();
        Account fromAccount = repo.findOneByName(operation.getAccountNames().get(0)).get();
        Account toAccount = repo.findOneByName(operation.getAccountNames().get(1)).get();

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        repo.save(toAccount);
        return repo.save(fromAccount);
    }

    private Account deposit(Operation operation) {
        assert operation.getOperationType() == OperationType.DEPOSIT;

        //TODO: avoid copy-paste
        return repo.findOneByName(operation.getAccountNames().get(0))
                .map(account -> {
                    BigDecimal newAmount = account.getBalance().add(operation.getAmount());
                    account.setBalance(newAmount);
                    return repo.saveAndFlush(account); //TODO: vs save?
                })
                .orElseThrow(NotFoundException::new);
    }

    private Account withdraw(Operation operation) {
        assert operation.getOperationType() == OperationType.WITHDRAWAL;
        return repo.findOneByName(operation.getAccountNames().get(0))
                .map(account -> {
                    BigDecimal newAmount = account.getBalance().subtract(operation.getAmount());
                    account.setBalance(newAmount);
                    return repo.saveAndFlush(account); //TODO: vs save?
                })
                .orElseThrow(NotFoundException::new);
    }

}
