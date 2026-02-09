package com.example.controller;

import com.example.entity.Address;
import com.example.entity.Student;
import com.example.entity.Subject;
import com.example.enums.SubjectNameFilter;
import com.example.model.*;
import com.example.response.StudentResponse;
import com.example.service.StudentService;
import com.example.util.CursorUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Controller
public class StudentController {
    private final StudentService studentService;

    @Transactional
    @QueryMapping(name = "student")
    StudentResponse getStudent(@Argument Long id) {
        log.info("QueryMapping: getStudent called with id {}", id);
        Student student = studentService.getStudent(id);
        return new StudentResponse(student);
    }

/*    @Transactional
    @QueryMapping(name = "students")
    List<StudentResponse> getStudents(@Argument String city) {
        log.info("QueryMapping: getStudents called");
        List<Student> studentList = studentService.getStudents(city);
        return studentList.stream().map(StudentResponse::new).toList();
    }*/

/*    @BatchMapping(typeName = "StudentResponse", field = "learningSubjects")
    Map<StudentResponse, List<Subject>> getLearningSubjects(List<StudentResponse> students,
                                                            @Argument(name = "subjectName") SubjectNameFilter subjectName) {
        log.info("getLearningSubjects for {} students", students.size());
        log.info("subjectName: {}", subjectName);

        List<Long> studentIds = students.stream().map(StudentResponse::getId).toList();
        List<Subject> subjects = studentService.getSubjectsByStudentIds(studentIds);

        Map<Long, StudentResponse> studentResponseMap = students
                .stream()
                .collect(Collectors.toMap(StudentResponse::getId, Function.identity()));

        return subjects
                .stream()
                .collect(Collectors.groupingBy(s -> studentResponseMap.get(s.getStudent().getId())));


    }*/

    @SchemaMapping
    List<Subject> learningSubjects(StudentResponse student, @Argument List<SubjectNameFilter> subjectNames) {
        log.info("Getting subjects for student {} with filter {}",
                student.getId(), subjectNames);

        return studentService.getSubjectsByStudentId(
                student.getId(),
                subjectNames
        );
    }

    @QueryMapping
    @Transactional
    StudentConnection students(@Argument Integer first, @Argument String after) {
        Long afterId = CursorUtil.decodeCursor(after);
        StudentPage page = studentService.getStudents(first, afterId);

        List<StudentEdge> edges = page.getStudents().stream()
                .map(student -> new StudentEdge(
                        student,
                        CursorUtil.encodeCursor(student.getId())
                ))
                .toList();

        String endCursor = edges.isEmpty() ? null :
                edges.get(edges.size() - 1).getCursor();

        return new StudentConnection(
                edges,
                new PageInfo(page.isHasNextPage(), null, null, endCursor), page.getCount());

    }

    @Transactional
    @QueryMapping(name = "allStudents")
    public List<StudentResponse> getAllStudents(@Argument Integer offset, @Argument Integer limit) {
        log.info("getAllStudents called");
        List<Student> studentList = studentService.getAllStudentsAgnostic(offset, limit);
        List<StudentResponse> studentResponseList = new ArrayList<>();

        studentList.forEach(student -> {
            studentResponseList.add(new StudentResponse(student));
        });

        return studentResponseList;
    }

    @MutationMapping
    public StudentResponse createStudentWithSubjects(
            @Argument CreateStudentWithSubjectsInput input
    ) {
        log.info("Creating student with {} subjects",
                input.getSubjects() != null ? input.getSubjects().size() : 0);

        Student student = new Student();
        student.setFirstName(input.getFirstName());
        student.setLastName(input.getLastName());
        student.setEmail(input.getEmail());

        Address address = new Address();
        address.setStreet(input.getStreet());
        address.setCity(input.getCity());
        student.setAddress(address);

        // Add subjects
        if (input.getSubjects() != null) {
            List<Subject> subjects = input.getSubjects().stream()
                    .map(subInput -> {
                        Subject subject = new Subject();
                        subject.setSubjectName(subInput.getSubjectName());
                        subject.setMarksObtained(subInput.getMarksObtained());
                        subject.setStudent(student);  // Set bidirectional relationship
                        return subject;
                    })
                    .toList();
            student.setLearningSubjects(subjects);
        }

        Student saved = studentService.createStudent(student);
        return new StudentResponse(saved);
    }

    @Transactional
    @MutationMapping
    public StudentResponse updateStudent(@Argument UpdateStudentInput input) {
        log.info("Updating student ID: {}", input.getId());

        Student existing = studentService.getStudentById(input.getId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + input.getId()));

        // Update only fields that are provided
        if (input.getFirstName() != null) {
            existing.setFirstName(input.getFirstName());
        }
        if (input.getLastName() != null) {
            existing.setLastName(input.getLastName());
        }
        if (input.getEmail() != null) {
            existing.setEmail(input.getEmail());
        }
        if (input.getStreet() != null || input.getCity() != null) {
            Address address = existing.getAddress() != null
                    ? existing.getAddress()
                    : new Address();

            if (input.getStreet() != null) {
                address.setStreet(input.getStreet());
            }
            if (input.getCity() != null) {
                address.setCity(input.getCity());
            }
            existing.setAddress(address);
        }

        if (input.getSubjects() != null && !input.getSubjects().isEmpty()) {
            List<Subject> newSubjects = new ArrayList<>();
            for (com.example.model.Subject subject : input.getSubjects()) {
                Subject newSubject = new Subject();
                newSubject.setSubjectName(subject.getSubjectName());
                newSubject.setMarksObtained(subject.getMarksObtained());
                newSubject.setStudent(existing);

                newSubjects.add(newSubject);
            }
            existing.getLearningSubjects().clear();
            existing.getLearningSubjects().addAll(newSubjects);
        }

        Student updated = studentService.updateStudent(existing);
        return new StudentResponse(updated);
    }

    @MutationMapping
    public DeleteResult deleteStudent(@Argument Long id) {
        log.info("Deleting student ID: {}", id);

        try {
            boolean existed = studentService.deleteStudent(id);

            if (existed) {
                return new DeleteResult(
                        true,
                        "Student deleted successfully",
                        id
                );
            } else {
                return new DeleteResult(
                        false,
                        "Student not found",
                        null
                );
            }
        } catch (Exception e) {
            log.error("Error deleting student", e);
            return new DeleteResult(
                    false,
                    "Error deleting student: " + e.getMessage(),
                    null
            );
        }
    }

}

