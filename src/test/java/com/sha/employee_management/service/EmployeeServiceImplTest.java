package com.sha.employee_management.service;

import com.sha.employee_management.dto.EmployeeDTO;
import com.sha.employee_management.entity.Employee;
import com.sha.employee_management.exception.ResourceNotFoundException;
import com.sha.employee_management.repository.EmployeeRepository;
import com.sha.employee_management.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee1;
    private Employee employee2;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setId(1L);
        employee1.setName("Alice Johnson");
        employee1.setEmail("alice@example.com");
        employee1.setDepartment("Engineering");

        employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Bob Smith");
        employee2.setEmail("bob@example.com");
        employee2.setDepartment("Marketing");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setName("Alice Johnson");
        employeeDTO.setEmail("alice@example.com");
        employeeDTO.setDepartment("Engineering");
    }

    @Test
    @DisplayName("Create employee successfully")
    void testSaveEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        EmployeeDTO savedDTO = employeeService.saveEmployee(employeeDTO);

        assertNotNull(savedDTO);
        assertEquals(1L, savedDTO.getId());
        assertEquals("Alice Johnson", savedDTO.getName());
        assertEquals("alice@example.com", savedDTO.getEmail());
        assertEquals("Engineering", savedDTO.getDepartment());

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Get all employees successfully")
    void testGetAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2));

        List<EmployeeDTO> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(2, employees.size());
        assertEquals("Alice Johnson", employees.get(0).getName());
        assertEquals("Bob Smith", employees.get(1).getName());

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Fetch paginated employees successfully")
    void testGetEmployeesPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(List.of(employee1, employee2), pageable, 2);

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        Page<EmployeeDTO> resultPage = employeeService.getEmployees(pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(1, resultPage.getTotalPages());
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Alice Johnson", resultPage.getContent().get(0).getName());

        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Update employee successfully when ID exists")
    void testUpdateEmployee_Success() {
        EmployeeDTO updateInfo = new EmployeeDTO();
        updateInfo.setName("Alice Updated");
        updateInfo.setEmail("alice.new@example.com");
        updateInfo.setDepartment("DevOps");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setName("Alice Updated");
        updatedEmployee.setEmail("alice.new@example.com");
        updatedEmployee.setDepartment("DevOps");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        EmployeeDTO result = employeeService.updateEmployee(1L, updateInfo);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice Updated", result.getName());
        assertEquals("alice.new@example.com", result.getEmail());
        assertEquals("DevOps", result.getDepartment());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update employee throws ResourceNotFoundException when ID does not exist")
    void testUpdateEmployee_NotFound() {
        EmployeeDTO updateInfo = new EmployeeDTO();
        updateInfo.setName("Nobody");
        updateInfo.setEmail("nobody@example.com");
        updateInfo.setDepartment("None");

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.updateEmployee(999L, updateInfo)
        );

        assertTrue(exception.getMessage().contains("Employee not found with id: 999"));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Delete employee successfully when ID exists")
    void testDeleteEmployee_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));

        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete employee throws ResourceNotFoundException when ID does not exist")
    void testDeleteEmployee_NotFound() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.deleteEmployee(999L)
        );

        assertTrue(exception.getMessage().contains("Employee not found with id: 999"));
        verify(employeeRepository, times(1)).existsById(999L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
