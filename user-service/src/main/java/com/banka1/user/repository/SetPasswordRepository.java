package com.banka1.user.repository;


import com.banka1.user.model.SetPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SetPasswordRepository extends JpaRepository<SetPassword, Long> {

    Optional<SetPassword> findByToken(String token);

}
