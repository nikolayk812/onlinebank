package com.onlinebank.service;

import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;

import java.util.List;


//TODO: javadoc
//TODO: readonly for some transactions
public interface AccountService {

    //TODO: optional or exception?
    Account get(String name);

    Account create(String name);

    void delete(String name);

    //TODO: pageable
    List<Account> getAll();

    Account processOperation(Operation operation);

}
