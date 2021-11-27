package org.secureapp.repository;

import org.secureapp.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsernameIgnoreCase(String username);

    User findByUsernameNotIgnoreCaseAndEmailIgnoreCase(String username, String email);

    List<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);


}
