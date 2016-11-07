package com.onlinebank.service;

import com.onlinebank.AppConfig;
import com.onlinebank.model.Account;
import com.onlinebank.model.operation.Operation;
import com.onlinebank.model.operation.Operations;
import com.onlinebank.service.exceptions.NotFoundException;
import com.onlinebank.service.exceptions.OperationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionSystemException;

import java.math.BigDecimal;
import java.util.List;

import static com.onlinebank.util.Constants.H2;
import static com.onlinebank.util.TestUtil.assertBigDecimalEquals;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

@ContextConfiguration(classes = AppConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Sql(scripts = "classpath:db/h2/initDB.sql")
@ActiveProfiles(H2)
public class AccountServiceImplTest {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImplTest.class);

    private static final int ACCOUNT_ID = 100;
    private static final int ACCOUNT_ID_2 = ACCOUNT_ID + 1;
    private static final String ACCOUNT_NAME = "first";
    private static final String ACCOUNT_NAME_2 = "second";

    @Autowired
    private AccountService service;

    @Test
    public void testCreate() throws Exception {
        Account account = service.create(ACCOUNT_NAME);
        assertAccountEquals(new Account(ACCOUNT_ID, BigDecimal.ZERO, ACCOUNT_NAME), account);
    }

    @Test
    public void testGet() throws Exception {
        Account created = service.create(ACCOUNT_NAME);
        Account retrieved = service.get(ACCOUNT_NAME);
        assertAccountEquals(created, retrieved);
    }

    @Test(expected = NotFoundException.class)
    public void testGetFail() throws Exception {
        Account account = service.get("Non-existing");
        log.info("{}", account);
    }

    @Test
    public void testGetAllSorted() throws Exception {
        service.create(ACCOUNT_NAME_2);
        service.create(ACCOUNT_NAME);
        List<Account> accounts = service.getAll();
        assertEquals("Wrong number of accounts", 2, accounts.size());
        assertEquals("Wrong account name", accounts.get(0).getName(), ACCOUNT_NAME);
        assertEquals("Wrong account name", accounts.get(1).getName(), ACCOUNT_NAME_2);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateDuplicateNameFail() throws Exception {
        Account created = service.create(ACCOUNT_NAME);
        assertEquals("Wrong account id", ACCOUNT_ID, created.getId().intValue());
        service.create(ACCOUNT_NAME.toUpperCase());
    }

    //TODO: Hibernate Validator throws javax.validation.ConstraintViolationException which is not org.hibernate.HibernateException
    //TODO: which results in wrong translation and weird TransactionSystemException being thrown
    @Test(expected = TransactionSystemException.class)
    public void testCreateIllegalNameFail() throws Exception {
        service.create("?%");
    }

    @Test
    public void testDeposit() throws Exception {
        service.create(ACCOUNT_NAME);

        BigDecimal amount = BigDecimal.valueOf(100);
        Operation deposit = Operations.deposit(ACCOUNT_NAME, amount);
        Account deposited = service.processOperation(deposit);
        Account expected = new Account(ACCOUNT_ID, amount, ACCOUNT_NAME);
        assertAccountEquals(expected, deposited);

        Account retrieved = service.get(ACCOUNT_NAME);
        assertAccountEquals(expected, retrieved);
    }

    @Test
    public void testWithdraw() throws Exception {
        service.create(ACCOUNT_NAME);
        BigDecimal depositAmount = BigDecimal.valueOf(100);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, depositAmount));

        BigDecimal withdrawalAmount = BigDecimal.valueOf(60);
        Account withdrawn = service.processOperation(Operations.withdraw(ACCOUNT_NAME, withdrawalAmount));
        BigDecimal balance = depositAmount.subtract(withdrawalAmount);
        Account expected = new Account(ACCOUNT_ID, balance, ACCOUNT_NAME);
        assertAccountEquals(expected, withdrawn);
    }

    @Test(expected = OperationException.class)
    public void testWithdrawNotEnoughBalanceFail() throws Exception {
        service.create(ACCOUNT_NAME);
        service.processOperation(Operations.withdraw(ACCOUNT_NAME, BigDecimal.TEN));
    }

    @Test
    public void testTransfer() throws Exception {
        service.create(ACCOUNT_NAME);
        service.create(ACCOUNT_NAME_2);

        BigDecimal depositAmount = BigDecimal.valueOf(100);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, depositAmount));
        BigDecimal transferAmount = BigDecimal.valueOf(70);
        Account first = service.processOperation(
                Operations.transfer(ACCOUNT_NAME, transferAmount, ACCOUNT_NAME_2));
        Account second = service.get(ACCOUNT_NAME_2);

        BigDecimal firstBalance = depositAmount.subtract(transferAmount);
        assertAccountEquals(new Account(ACCOUNT_ID, firstBalance, ACCOUNT_NAME), first);
        assertAccountEquals(new Account(ACCOUNT_ID_2, transferAmount, ACCOUNT_NAME_2), second);
    }

    @Test(expected = OperationException.class)
    public void testTransferNotEnoughBalanceFail() throws Exception {
        service.create(ACCOUNT_NAME);
        service.create(ACCOUNT_NAME_2);

        service.processOperation(Operations.deposit(ACCOUNT_NAME_2, BigDecimal.ONE));
        service.processOperation(Operations.transfer(ACCOUNT_NAME_2, BigDecimal.TEN, ACCOUNT_NAME));
    }

    @Test(expected = OperationException.class)
    public void testTransferToNonExistingAccountFail() throws Exception {
        service.create(ACCOUNT_NAME);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, BigDecimal.valueOf(20)));
        service.processOperation(Operations.transfer(ACCOUNT_NAME, BigDecimal.TEN, "non-existing"));
    }

    @Test
    public void testDelete() throws Exception {
        service.create(ACCOUNT_NAME);
        service.delete(ACCOUNT_NAME);
        assertEquals(0, service.getAll().size());
    }

    @Test
    public void testDeleteNonExistingAccount() throws Exception {
        service.delete("non-existing");

    }

    @Test
    public void testDepositNonExistingAccountThenCreate() throws Exception {
        String name = "non-existing";
        try {
            service.processOperation(Operations.deposit(name, BigDecimal.TEN));
        } catch (OperationException e) {
            Account created = service.create(name);
            assertAccountEquals(new Account(ACCOUNT_ID, BigDecimal.ZERO, name), created);
            return;
        }
        fail();
    }

    private static void assertAccountEquals(Account expected, Account actual) {
        assertEquals("Wrong account name", expected.getName(), actual.getName());
        assertEquals("Wrong account id", expected.getId(), actual.getId());
        assertBigDecimalEquals("Wrong balance", expected.getBalance(), actual.getBalance());
    }
}