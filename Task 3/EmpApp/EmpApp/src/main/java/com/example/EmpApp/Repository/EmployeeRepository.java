package com.example.EmpApp.Repository;

import com.example.EmpApp.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Add these custom query methods
    boolean existsByEmail(String email);
    boolean existsByPhoneNo(String phoneNo);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.email = :email OR e.phoneNo = :phoneNo")
    boolean existsByEmailOrPhoneNo(String email, String phoneNo);

    boolean existsByEmailAndEmpIdNot(String email, Long empId);
    boolean existsByPhoneNoAndEmpIdNot(String phoneNo, Long empId);

    @Query("SELECT e FROM Employee e WHERE " +
            "e.empName = :empName AND " +
            "e.email = :email AND " +
            "e.phoneNo = :phoneNo AND " +
            "e.address.street = :street AND " +
            "e.address.city = :city AND " +
            "e.address.state = :state AND " +
            "e.address.zipCode = :zipCode")
    Optional<Employee> findExactDuplicate(
            String empName,
            String email,
            String phoneNo,
            String street,
            String city,
            String state,
            String zipCode
    );

    List<Employee> findAllByOrderByEmpIdAsc();

    Optional<Employee> findByEmail(String email);
}