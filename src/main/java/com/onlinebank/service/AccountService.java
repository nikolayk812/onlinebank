package com.onlinebank.service;

import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;
import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;

import java.util.List;


/**
 * Account service interface.
 * All methods should not accept either return {@code null} values.
 */
public interface AccountService {

    /**
     * Retrieves account by name
     *
     * @param name account name
     * @return account
     * @throws NotFoundException in case account is not found
     */
    Account get(String name) throws NotFoundException;

    /**
     * Creates account with name provided
     *
     * @param name account name
     * @return account
     */
    Account create(String name);

    /**
     * Deletes account by name provided
     *
     * @param name account name
     * @throws NotFoundException in case account does not exist
     */
    void delete(String name) throws NotFoundException;

    /**
     * Retrieves all accounts
     *
     * @return list of accounts, ASC-sorted by account name
     */
    List<Account> getAll();

    /**
     * Processes operation at account(s)
     *
     * @param operation account operation
     * @return source account, i.e. for TRANSFER operation account from which money are being transferred
     * @throws OperationException in case operation fails due to business constraints
     */
    Account processOperation(Operation operation) throws OperationException;

}
