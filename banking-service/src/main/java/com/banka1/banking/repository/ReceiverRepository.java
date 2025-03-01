package com.banka1.banking.repository;

import com.banka1.banking.models.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
}
