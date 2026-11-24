package com.sha.employee_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sha.employee_management.controller.EmployeeController;
import com.sha.employee_management.dto.EmployeeDTO;
import com.sha.employee_management.exception.GlobalExceptionHandler;
import com.sha.employee_management.exception.ResourceNotFoundException;
import com.sha.employee_management.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeValidationAndExceptionTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateEmployee_ValidationFailure_BlankFields() throws Exception {
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setName("");
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setDepartment("");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.department").exists());
    }

    @Test
    void testCreateEmployee_Success() throws Exception {
        EmployeeDTO validDTO = new EmployeeDTO(null, "Alice Smith", "alice@example.com", "Engineering");
        EmployeeDTO createdDTO = new EmployeeDTO(1L, "Alice Smith", "alice@example.com", "Engineering");

        when(employeeService.saveEmployee(any(EmployeeDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testUpdateEmployee_NotFound_Returns404() throws Exception {
        EmployeeDTO updateDTO = new EmployeeDTO(null, "Bob", "bob@example.com", "Sales");

        when(employeeService.updateEmployee(eq(999L), any(EmployeeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        mockMvc.perform(put("/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 999"));
    }
}
