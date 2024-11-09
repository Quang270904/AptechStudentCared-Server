package com.example.aptechstudentcaredserver.controller;

import com.example.aptechstudentcaredserver.bean.request.StudentRequest;
import com.example.aptechstudentcaredserver.bean.response.ImportResponse;
import com.example.aptechstudentcaredserver.bean.response.ResponseMessage;
import com.example.aptechstudentcaredserver.bean.response.StudentResponse;
import com.example.aptechstudentcaredserver.bean.response.SubjectInfoResponse;
import com.example.aptechstudentcaredserver.enums.ClassMemberStatus;
import com.example.aptechstudentcaredserver.service.StudentService;
import com.example.aptechstudentcaredserver.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/students")
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> students = studentService.findAllStudent(pageable);
        return ResponseEntity.ok(students);
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsByStatus(@PathVariable("status") ClassMemberStatus status) {
        List<StudentResponse> students = studentService.findStudentsByStatus(status);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO')")
    public ResponseEntity<String> addStudent(@RequestBody StudentRequest studentRq) {
        try {
            studentService.createStudent(studentRq);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"message\": \"Student added successfully\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO')")
    public ResponseEntity<String> importStudents(@ModelAttribute("file") MultipartFile file) {
        try {
            List<ImportResponse> importResults = ExcelUtils.parseStudentExcelFile(file,studentService);
            StringBuilder errorMessage = new StringBuilder();

            for (ImportResponse result : importResults) {
                String message = result.getMessage();
                int rowNumber = result.getRowNumber();

                if (message.startsWith("Success")) {
                    // Success messages are handled by the importResults itself
                    continue;
                } else {
                    // Append error details including the row number
                    errorMessage.append("Row ").append(rowNumber).append(": ").append(message).append("\n");
                }
            }

            // Nếu không có lỗi nào được ghi lại, trả về thông báo thành công toàn bộ
            if (errorMessage.length() == 0) {
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("{\"message\": \"All records in the file were processed successfully without any errors.\"}");
            } else {
                // Nếu có lỗi, trả về thông báo lỗi
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<StudentResponse> getStudentInfo(@PathVariable("studentId") int studentId) {
        StudentResponse studentResponse = studentService.findStudentById(studentId);
        return ResponseEntity.ok(studentResponse);
    }

    @PutMapping("/{studentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable int studentId,
            @RequestBody StudentRequest studentRequest) {
        StudentResponse updatedStudent = studentService.updateStudent(studentId, studentRequest);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SRO')")
    public ResponseEntity<String> deleteStudent(@PathVariable("studentId") int studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body("{\"message\": \"Student deleted successfully\"}");
    }

}
