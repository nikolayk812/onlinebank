package com.onlinebank.web.rest;


import com.onlinebank.model.Account;
import com.onlinebank.model.operation.OperationImpl;
import com.onlinebank.model.operation.Operations;
import com.onlinebank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.onlinebank.web.rest.AccountResource.URL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
//TODO: delete JSON_VALUE because RestController already mean that?
@RequestMapping(value = URL, produces = APPLICATION_JSON_VALUE)
public class AccountResource {
    public static final String URL = "/rest/accounts";

    @Autowired
    private AccountService service;

    @GetMapping(value = "/{name}")
    public Account get(@PathVariable("name") String name) {
        return service.get(name);
    }

    @PutMapping(value = "/{name}", consumes = APPLICATION_JSON_VALUE)
    public Account operation(@PathVariable("name") String name,
                             @RequestBody @Valid OperationDTO operation) {
        //TODO: validate name matches!

        return service.processOperation(
                Operations.create(operation.getOperationType(),
                        operation.getAccountNames(), operation.getAmount()));
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody String name) {
        Account createdAccount = service.create(name);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{name}", consumes = APPLICATION_JSON_VALUE)
    public void delete(@RequestBody String name) {
        //TODO: validate name matches!

        service.delete(name);
    }

    private static HttpHeaders createTextPlainHeaders() {
        HttpHeaders textPlainHeaders = new HttpHeaders();
        textPlainHeaders.setContentType(MediaType.TEXT_PLAIN);
        return textPlainHeaders;
    }

    //TODO: enable paging
    /*@GetMapping
    public ResponseEntity<List<Account>> getAll(Pageable pageable) throws URISyntaxException {
        Page<Account> page = repo.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    */

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }

}
