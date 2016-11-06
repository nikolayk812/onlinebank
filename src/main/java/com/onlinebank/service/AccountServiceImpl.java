package com.onlinebank.service;

import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;
import com.onlinebank.repo.AccountRepository;
import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;
import com.onlinebank.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

//TODO: add validation
//TODO: dirty checks?
//TODO: account name transformer/strategy

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AccountServiceImpl implements AccountService {
    private final static Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final LockService<Integer, Account> lockService;
    private final AccountRepository repo;
    private final OperationExecutor operationExecutor;

    @Autowired
    public AccountServiceImpl(LockService<Integer, Account> lockService, AccountRepository repo, OperationExecutor operationExecutor) {
        this.lockService = lockService;
        this.repo = repo;
        this.operationExecutor = operationExecutor;
    }

    @Transactional(readOnly = true)
    @Override
    public Account get(String name) {
        String lowerName = name.toLowerCase();
        Integer id = repo.resolve(singletonList(lowerName)).get(lowerName);
        if (id == null)
            throw new NotFoundException("Account " + lowerName + " does not exist");

        return lockService.runUnderReadLocks(singletonList(id), () -> {
            //noinspection CodeBlock2Expr
            return repo.findOneByName(lowerName)
                .orElseThrow(() ->
                        new NotFoundException("Account " + lowerName + " does not exist"));
        });
    }

    @Override
    public Account create(String name) {
        Account account = new Account(name.toLowerCase());
        return repo.save(account);
    }

    @Override
    public void delete(String name) {
        String lowerName = name.toLowerCase();
        Integer id = repo.resolve(singletonList(lowerName)).get(lowerName);
        if (id == null)
            return;
        lockService.runUnderWriteLocks(singletonList(id), () -> {
            repo.delete(id);
            return null;
        });
    }

    //TODO: overrides properties sor not?
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    @Override
    public List<Account> getAll() {
        //TODO: take all locks?
        return repo.findAll();
    }

    @Override
    public Account processOperation(Operation operation) {
        //TODO: lowerNames!!!

        Map<String, Integer> resolvedIds = repo.resolve(operation.getAccountNames());
        if (operation.getAccountNames().size() > resolvedIds.size())
            throw new OperationException("Some accounts of " +
                    operation.getAccountNames() + " do not exist");

        return lockService.runUnderWriteLocks(resolvedIds.values(),
                () -> operationExecutor.execute(operation));

    }

}
