package com.onlinebank.web.rest;

import com.onlinebank.AppConfig;
import com.onlinebank.LocalAppConfig;
import com.onlinebank.model.operation.OperationType;
import com.onlinebank.model.operation.Operations;
import com.onlinebank.service.AccountService;
import com.onlinebank.util.Constants;
import com.onlinebank.util.TestUtil;
import com.onlinebank.web.WebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {AppConfig.class, LocalAppConfig.class, WebConfig.class})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(Constants.H2)
@Sql(scripts = "classpath:db/h2/initDB.sql")
public class AccountResourceTest {
    private static final String ACCOUNT_NAME = "first";
    private static final String ACCOUNT_NAME_2 = "second";

    //TODO: delete?
    private static final CharacterEncodingFilter CHARACTER_ENCODING_FILTER = new CharacterEncodingFilter();

    static {
        CHARACTER_ENCODING_FILTER.setEncoding("UTF-8");
        CHARACTER_ENCODING_FILTER.setForceEncoding(true);
    }

    private MockMvc mockMvc;

    @Autowired
    private AccountService service;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @PostConstruct
    void postConstruct() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(CHARACTER_ENCODING_FILTER)
                .build();
    }

    @Test
    public void testGetRootEmpty() throws Exception {
        TestUtil.print(mockMvc.perform(get(AccountResource.URL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetRoot() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(get(AccountResource.URL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$[0].balance", TestUtil.closeTo(0d)));

        service.create(ACCOUNT_NAME_2);
        TestUtil.print(mockMvc.perform(get(AccountResource.URL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$[1].name", is(ACCOUNT_NAME_2)));
    }

    @Test
    public void testCreate() throws Exception {
        TestUtil.print(mockMvc.perform(post(AccountResource.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACCOUNT_NAME)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME)));
                //.andExpect(jsonPath("$.balance", TestUtil.closeTo(0d))); TODO: fix
        ;
    }

    @Test
    public void testCreateIllegalName() throws Exception {
        TestUtil.print(mockMvc.perform(post(AccountResource.URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("?%")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmptyContent() throws Exception {
        TestUtil.print(mockMvc.perform(post(AccountResource.URL)
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(delete(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACCOUNT_NAME)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyOrNullString()));
    }

    @Test
    public void testDeleteWrongContent() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(delete(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACCOUNT_NAME_2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteNonExisting() throws Exception {
        TestUtil.print(mockMvc.perform(delete(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACCOUNT_NAME)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetByName() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(get(AccountResource.URL + "/" + ACCOUNT_NAME)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$.balance", TestUtil.closeTo(0d)));
    }

    @Test
    public void testGetByNonExistingName() throws Exception {
        TestUtil.print(mockMvc.perform(get(AccountResource.URL + "/non-existing")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeposit() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.deposit(ACCOUNT_NAME, BigDecimal.TEN)))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$.balance", TestUtil.closeTo(10d)));
    }

    @Test
    public void testNegativeDeposit() throws Exception {
        service.create(ACCOUNT_NAME);
        OperationDTO depositDTO = new OperationDTO(singletonList(ACCOUNT_NAME),
                BigDecimal.valueOf(-10), OperationType.DEPOSIT);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(depositDTO))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDepositOtherAccount() throws Exception {
        service.create(ACCOUNT_NAME);
        service.create(ACCOUNT_NAME_2);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.deposit(ACCOUNT_NAME_2, BigDecimal.valueOf(6))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDepositNonExistingAccount() throws Exception {
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.deposit(ACCOUNT_NAME, BigDecimal.valueOf(6))))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testWithdraw() throws Exception {
        service.create(ACCOUNT_NAME);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, BigDecimal.TEN));
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.withdraw(ACCOUNT_NAME, BigDecimal.valueOf(6))))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$.balance", TestUtil.closeTo(4d)));
    }

    @Test
    public void testWithdrawNotEnoughBalance() throws Exception {
        service.create(ACCOUNT_NAME);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.withdraw(ACCOUNT_NAME, BigDecimal.TEN))))
                .andExpect(status().isForbidden()));
    }

    @Test
    public void testTransfer() throws Exception {
        service.create(ACCOUNT_NAME);
        service.create(ACCOUNT_NAME_2);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, BigDecimal.valueOf(20)));

        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.transfer(ACCOUNT_NAME, BigDecimal.TEN, ACCOUNT_NAME_2))))
                .andExpect(status().isOk()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME)))
                .andExpect(jsonPath("$.balance", TestUtil.closeTo(10)));

        TestUtil.print(mockMvc.perform(get(AccountResource.URL + "/" + ACCOUNT_NAME_2))
                .andExpect(status().isOk()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.name", is(ACCOUNT_NAME_2)))
                .andExpect(jsonPath("$.balance", TestUtil.closeTo(10)));
    }

    @Test
    public void testTransferSameAccount() throws Exception {
        service.create(ACCOUNT_NAME);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, BigDecimal.TEN));

        OperationDTO transferDTO = new OperationDTO(asList(ACCOUNT_NAME, ACCOUNT_NAME),
                BigDecimal.TEN, OperationType.TRANSFER);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(transferDTO))))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testTransferSingleAccount() throws Exception {
        service.create(ACCOUNT_NAME);
        service.processOperation(Operations.deposit(ACCOUNT_NAME, BigDecimal.TEN));

        OperationDTO transferDTO = new OperationDTO(singletonList(ACCOUNT_NAME),
                BigDecimal.TEN, OperationType.TRANSFER);
        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(transferDTO))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTransferNotEnoughBalance() throws Exception {
        service.create(ACCOUNT_NAME);
        service.create(ACCOUNT_NAME_2);

        TestUtil.print(mockMvc.perform(put(AccountResource.URL + "/" + ACCOUNT_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(Operations.transfer(ACCOUNT_NAME, BigDecimal.TEN, ACCOUNT_NAME_2))))
                .andExpect(status().isForbidden()));
    }

}