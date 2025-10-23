package com.carpool.demo.data.repository;

import com.carpool.demo.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByMobileNumber(String mobileNumber);
    User findByToken(String token);
}
