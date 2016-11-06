package com.onlinebank.repo;

import com.onlinebank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.StringQueryParameterBinder;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findOneByName(String name);

    //prohibited to use by requirements
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Account findAndLockById(Integer id);

    @Query("SELECT id, name FROM Account a WHERE name IN (:names)")
    List<Object[]> findIdsByNames(@Param("names") List<String> names);

    default Map<String, Integer> resolve(List<String> names) {
        return findIdsByNames(names).stream()
                .collect(toMap(
                        o -> (String) o[1],
                        o -> (Integer) o[0]
                ));
    }

}
