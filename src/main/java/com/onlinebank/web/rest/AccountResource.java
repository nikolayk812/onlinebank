package com.onlinebank.web.rest;


import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operations;
import com.onlinebank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.onlinebank.web.rest.AccountResource.URL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
//TODO: delete JSON_VALUE because RestController already mean that?
@RequestMapping(value = URL, produces = APPLICATION_JSON_VALUE)
public class AccountResource {
    static final String URL = "/rest/accounts";

    @Autowired
    private AccountService service;

    @GetMapping(value = "/{name}")
    public Account get(@PathVariable("name") String name) {
        return service.get(name);
    }

    @PutMapping(value = "/{name}", consumes = APPLICATION_JSON_VALUE)
    public Account operation(@PathVariable("name") String resourceName,
                             @RequestBody @Valid OperationDTO operation) {
        double accountsSize = operation.getAccountNames().size();
        if (accountsSize == 0 || accountsSize > 2)
            throw new IllegalArgumentException("Wrong number of accounts " + accountsSize +
                    " in operation " + operation.getOperationType() + ". Allowed values: 1 or 2.");

        String sourceAccountName = operation.getAccountNames().get(0);
        if (!sourceAccountName.equalsIgnoreCase(resourceName))
            throw new IllegalArgumentException("Operation account name '" + sourceAccountName +
                    "' and resource name: '" + resourceName + "' do not match");

        return service.processOperation(
                Operations.create(operation.getOperationType(),
                        operation.getAccountNames(), operation.getAmount()));
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody String name) {
        Account createdAccount = service.create(name);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{name}")
    public ResponseEntity<Void> delete(@PathVariable("name") String resourceName,
                       @RequestBody String accountName) {
        if (!accountName.equalsIgnoreCase(resourceName))
            throw new IllegalArgumentException("Cannot delete account '" + resourceName +
                    "' because payload name does not match: '" + accountName + "'");

        service.delete(resourceName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }

}
