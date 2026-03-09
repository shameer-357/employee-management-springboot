package com.sha.employee_management.service.impl;

import com.sha.employee_management.dto.EmployeeDTO;
import com.sha.employee_management.entity.Employee;
import com.sha.employee_management.repository.EmployeeRepository;
import com.sha.employee_management.service.EmployeeService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeServiceImpl(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmployeeDTO saveEmployee(EmployeeDTO dto) {

        Employee employee = new Employee();

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());

        Employee saved = repository.save(employee);

        return mapToDTO(saved);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EmployeeDTO> getEmployees(Pageable pageable) {

        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());

        Employee updated = repository.save(employee);

        return mapToDTO(updated);
    }

    @Override
    public void deleteEmployee(Long id) {

        repository.deleteById(id);
    }

    private EmployeeDTO mapToDTO(Employee employee){

        EmployeeDTO dto = new EmployeeDTO();

        dto.setId(employee.getId());   // IMPORTANT
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());

        return dto;
    }
}