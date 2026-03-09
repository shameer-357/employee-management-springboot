package com.sha.employee_management.controller;

import com.sha.employee_management.dto.EmployeeDTO;
import com.sha.employee_management.entity.Employee;
import com.sha.employee_management.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping("/page")
    public Page<EmployeeDTO> getEmployees(Pageable pageable) {
        return service.getEmployees(pageable);
    }

    @PostMapping
    public EmployeeDTO createEmployee(@RequestBody EmployeeDTO employeeDTO){
        return service.saveEmployee(employeeDTO);
    }

    @GetMapping
    public List<EmployeeDTO> getEmployees() {
        return service.getAllEmployees();
    }

    @PutMapping("/{id}")
    public EmployeeDTO updateEmployee(@PathVariable Long id,
                                      @RequestBody EmployeeDTO employeeDTO) {
        return service.updateEmployee(id, employeeDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
    }
}