package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long>

{
    Optional<Role> findByRoleName(String roleName);
}
