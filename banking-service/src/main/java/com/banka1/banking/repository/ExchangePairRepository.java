package com.banka1.banking.repository;

import com.banka1.banking.models.ExchangePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangePairRepository extends JpaRepository<ExchangePair, Long> {
}
