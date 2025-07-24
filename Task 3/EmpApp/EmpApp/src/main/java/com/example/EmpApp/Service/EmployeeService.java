package com.example.EmpApp.Service;

import com.example.EmpApp.dto.AddressDTO;
import com.example.EmpApp.dto.EmployeeDTO;
import com.example.EmpApp.Entity.*;
import com.example.EmpApp.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class EmployeeService implements EmployeeServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepo;
    private final AddressRepository addressRepo;
    private final DepartmentRepository departmentRepo;
    private final ProjectRepository projectRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepo,
                           AddressRepository addressRepo,
                           DepartmentRepository departmentRepo,
                           ProjectRepository projectRepo,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.addressRepo = addressRepo;
        this.departmentRepo = departmentRepo;
        this.projectRepo = projectRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Employee create(EmployeeDTO dto) {
        logger.debug("Creating employee with email: {}", dto.getEmail());

        if (employeeRepo.existsByEmailOrPhoneNo(dto.getEmail(), dto.getPhoneNo())) {
            throw new RuntimeException("Employee with same email or phone already exists");
        }

        AddressDTO addrDto = dto.getAddress();
        employeeRepo.findExactDuplicate(
                dto.getEmpName(), dto.getEmail(), dto.getPhoneNo(),
                addrDto.getStreet(), addrDto.getCity(), addrDto.getState(), addrDto.getZipCode()
        ).ifPresent(e -> {
            throw new RuntimeException("Duplicate employee with same contact and address exists");
        });

        Address address = addressRepo.findByStreetAndCityAndStateAndZipCode(
                addrDto.getStreet(), addrDto.getCity(), addrDto.getState(), addrDto.getZipCode()
        ).orElseGet(() -> {
            Address newAddr = new Address();
            newAddr.setStreet(addrDto.getStreet());
            newAddr.setCity(addrDto.getCity());
            newAddr.setState(addrDto.getState());
            newAddr.setZipCode(addrDto.getZipCode());
            return addressRepo.save(newAddr);
        });

        Department dept = departmentRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee emp = new Employee();
        emp.setEmpName(dto.getEmpName());
        emp.setEmail(dto.getEmail());
        emp.setPhoneNo(dto.getPhoneNo());
        emp.setPassword(passwordEncoder.encode(dto.getPassword()));
        emp.setAddress(address);
        emp.setDepartment(dept);

        if (dto.getProjectIds() != null && !dto.getProjectIds().isEmpty()) {
            Set<Project> projects = new HashSet<>(projectRepo.findAllById(dto.getProjectIds()));
            emp.setProjects(projects);
        }

        return employeeRepo.save(emp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAll() {
        return employeeRepo.findAllByOrderByEmpIdAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Override
    public Employee update(Long id, EmployeeDTO dto) {
        logger.debug("Updating employee with id: {}", id);

        Employee emp = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        AddressDTO addrDto = dto.getAddress();
        Address address = addressRepo.findByStreetAndCityAndStateAndZipCode(
                addrDto.getStreet(), addrDto.getCity(), addrDto.getState(), addrDto.getZipCode()
        ).orElseGet(() -> {
            Address newAddr = new Address();
            newAddr.setStreet(addrDto.getStreet());
            newAddr.setCity(addrDto.getCity());
            newAddr.setState(addrDto.getState());
            newAddr.setZipCode(addrDto.getZipCode());
            return addressRepo.save(newAddr);
        });

        Department dept = departmentRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        emp.setEmpName(dto.getEmpName());
        emp.setEmail(dto.getEmail());
        emp.setPhoneNo(dto.getPhoneNo());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()
                && !passwordEncoder.matches(dto.getPassword(), emp.getPassword())) {
            emp.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        emp.setAddress(address);
        emp.setDepartment(dept);

        if (dto.getProjectIds() != null) {
            Set<Project> projects = new HashSet<>(projectRepo.findAllById(dto.getProjectIds()));
            emp.setProjects(projects);
        }

        return employeeRepo.save(emp);
    }

    @Override
    public void delete(Long id) {
        if (!employeeRepo.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepo.deleteById(id);
        logger.info("Deleted employee with id: {}", id);
    }
}