package org.secureapp.repository;

import org.secureapp.model.TokenStore;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BlackListedTokenRepository extends CrudRepository<TokenStore, Long> {
    TokenStore findByToken(String token);
}
