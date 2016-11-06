package com.onlinebank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.onlinebank.util.Constants;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

//TODO: log4jdbc
//TODO: add immediate validation

@Entity
@Table(name = "accounts")
@Access(AccessType.FIELD)
public class Account {

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @JsonIgnore
    private Integer id;

    @Column(name = "balance", nullable = false)
    @DecimalMin(value = "0.00")
    @Digits(integer = 14, fraction = 10) //TODO: check!
    @NotNull
    private BigDecimal balance;

    @NotEmpty
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Column(name = "name", unique = true, nullable = false, updatable = false)
    private String name;

    public Account() {
    }

    public Account(String name) {
        this.balance = BigDecimal.ZERO;
        this.name = name;
    }

    public Account(Integer id, BigDecimal balance, String name) {
        this.id = id;
        this.balance = balance;
        this.name = name;
    }

    //TODO: JSon
//    public boolean isNew() {
//        return id == null;
//    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        Preconditions.checkArgument(balance.compareTo(BigDecimal.ZERO) >= 0,
                "Negative balance " + balance + " for account " + id + "/" + name + " not allowed");
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //TODO: equals and hashCode


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Account{");
        sb.append("id=").append(id);
        sb.append(", balance=").append(balance);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
