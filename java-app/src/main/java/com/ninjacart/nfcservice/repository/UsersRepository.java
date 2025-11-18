package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

    List<Users> findAllByNameInAndDeactivated(List<String> users, int deactivated);
}
