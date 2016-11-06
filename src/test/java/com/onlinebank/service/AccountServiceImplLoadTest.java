package com.onlinebank.service;

import com.onlinebank.AppConfig;
import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.onlinebank.util.AssertUtil.assertBigDecimalEquals;
import static com.onlinebank.util.Constants.H2;

@ContextConfiguration(classes = AppConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Sql(scripts = "classpath:db/h2/initDB.sql")
@ActiveProfiles(H2)
public class AccountServiceImplLoadTest {
    private static final int MAX_ITERATION = 2 * 50;

    private ExecutorService executorService;

    @Autowired
    private AccountService service;

    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(4);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void testConsistencyAndNoDeadlock() throws Exception {
        Account first = service.create("first");
        Account second = service.create("second");

        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        service.processOperation(Operations.deposit(first.getName(), depositAmount));
        service.processOperation(Operations.deposit(second.getName(), depositAmount));

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < MAX_ITERATION; i++) {
            if (i % 2 == 0) {
                futures.add(createTask(first.getName(), BigDecimal.TEN, second.getName()));
            } else {
                futures.add(createTask(second.getName(), BigDecimal.TEN, first.getName()));
            }
        }

        //blocking
        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[futures.size()])).get();

        first = service.get(first.getName());
        second = service.get(second.getName());

        System.out.println(first.getBalance() + " vs " + second.getBalance());

        assertBigDecimalEquals("First account has wrong balance", depositAmount, first.getBalance());
        assertBigDecimalEquals("Second account has wrong balance", depositAmount, second.getBalance());
    }

    private CompletableFuture<Void> createTask(String fromAccount, BigDecimal amount, String toAccount) {
        return CompletableFuture.supplyAsync(() -> {
            service.processOperation(Operations.transfer(fromAccount, amount, toAccount));
            return null;
        }, executorService);
    }

}
