package com.example.controller;

import com.example.entity.Student;
import com.example.entity.Subject;
import com.example.enums.SubjectNameFilter;
import com.example.response.StudentResponse;
import com.example.service.StudentService;
import com.example.util.CursorUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
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
}
