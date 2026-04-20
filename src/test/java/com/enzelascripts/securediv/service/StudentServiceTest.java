package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.StudentRequest;
import com.enzelascripts.securediv.response.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepo studentRepo;

    @Mock
    private CourseResultRepo courseResultRepo;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private StudentRequest studentRequest;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setStudentId("STU-001");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setGender("Male");
        student.setEmail("john.doe@university.edu");
        student.setPhoneNumber("+2348012345678");
        student.setDateOfBirth(LocalDate.of(2000, 1, 15));

        studentRequest = new StudentRequest();
        studentRequest.setStudentId("STU-001");
        studentRequest.setFirstName("John");
        studentRequest.setLastName("Doe");
        studentRequest.setGender("Male");
        studentRequest.setEmail("john.doe@university.edu");
        studentRequest.setPhoneNumber("+2348012345678");
        studentRequest.setDateOfBirth(LocalDate.of(2000, 1, 15));
    }

    @Test
    void createStudent_validRequest_returnsStudentResponse() {
        when(studentRepo.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.createStudent(studentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo("STU-001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        verify(studentRepo, times(1)).save(any(Student.class));
    }

    @Test
    void createStudent_nullRequest_throwsException() {
        assertThatThrownBy(() -> studentService.createStudent(null))
                .isInstanceOf(NullPointerException.class);

        verify(studentRepo, never()).save(any());
    }

    @Test
    void getAllStudents_returnsListOfResponses() {
        Student second = new Student();
        second.setStudentId("STU-002");
        second.setFirstName("Jane");
        second.setLastName("Smith");

        when(studentRepo.findAll()).thenReturn(List.of(student, second));

        List<StudentResponse> result = studentService.getAllStudents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStudentId()).isEqualTo("STU-001");
        assertThat(result.get(1).getStudentId()).isEqualTo("STU-002");
    }

    @Test
    void getAllStudents_emptyList_returnsEmpty() {
        when(studentRepo.findAll()).thenReturn(List.of());

        List<StudentResponse> result = studentService.getAllStudents();

        assertThat(result).isEmpty();
    }

    @Test
    void getStudentByStudentId_existingId_returnsStudent() {
        when(studentRepo.findStudentByStudentId("STU-001")).thenReturn(Optional.of(student));

        Student result = studentService.getStudentByStudentId("STU-001");

        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo("STU-001");
    }

    @Test
    void getStudentByStudentId_notFound_throwsResourceNotFoundException() {
        when(studentRepo.findStudentByStudentId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentByStudentId("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void getStudentAsResponse_existingId_returnsResponse() {
        when(studentRepo.findStudentByStudentId("STU-001")).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentAsResponse("STU-001");

        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo("STU-001");
    }

    @Test
    void deleteStudent_existingId_deletesStudentAndCourseResults() {
        List<CourseResult> courseResults = List.of(new CourseResult(), new CourseResult());

        when(studentRepo.findStudentByStudentId("STU-001")).thenReturn(Optional.of(student));
        when(courseResultRepo.getCourseResultsByStudent_Id(1L)).thenReturn(courseResults);

        studentService.deleteStudent("STU-001");

        verify(courseResultRepo).deleteAll(courseResults);
        verify(studentRepo).deleteByStudentId("STU-001");
    }

    @Test
    void deleteStudent_studentNotFound_throwsResourceNotFoundException() {
        when(studentRepo.findStudentByStudentId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courseResultRepo, never()).deleteAll(any());
        verify(studentRepo, never()).deleteByStudentId(any());
    }
}
