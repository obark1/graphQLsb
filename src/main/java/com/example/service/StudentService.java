package com.example.service;

import com.example.controller.StudentPage;
import com.example.entity.Address;
import com.example.entity.Student;
import com.example.entity.Subject;
import com.example.enums.SubjectNameFilter;
import com.example.repository.AddressRepository;
import com.example.repository.StudentRepository;
import com.example.repository.SubjectRepository;
import com.example.request.CreateStudentRequest;
import com.example.request.CreateSubjectRequest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StudentService {

    private final EntityManager entityManager;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    SubjectRepository subjectRepository;

    public Student getStudent(Long id) {
        return studentRepository.findById(id).orElseThrow();

    }

    public List<Student> getAllStudents(int offset, int limit) {
        return studentRepository.findAllWithOffsetAndLimit(offset, limit);
    }

    @Transactional
    public List<Student> getStudents(String city) {
        return studentRepository.findByAddressCity(city);
    }

    public String getFirstNameById(long id) {
        return studentRepository.findById(id).get().getFirstName();
    }

    public String getLastNameById(long id) {
        return studentRepository.findById(id).get().getLastName();
    }

    public List<Subject> getSubjectsByStudentId(long studentId, List<SubjectNameFilter> subjectNames) {
        return subjectRepository.findAllByStudentId(studentId)
                .stream()
                .filter(e -> subjectNames
                        .stream()
                        .map(Enum::name)
                        .toList()
                        .contains(e.getSubjectName()))
                .toList();
    }

    public List<Subject> getSubjectsByStudentIds(List<Long> studentIds) {
        return subjectRepository.findAllByStudentIdIn(studentIds);
    }

    @Transactional
    public List<Student> getAllStudentsAgnostic(int offset, int limit) {
        List<Student> query = entityManager.createQuery("SELECT s FROM Student s ORDER BY s.id", Student.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        return query;
    }

    public StudentPage getStudents(Integer first, Long afterId) {
        int limit = first != null ? first : 10;
        Pageable pageable = PageRequest.of(0, limit + 1);  // +1 to check hasNext

        Page<Student> students = afterId == null
                ? studentRepository.findAll(pageable)
                : studentRepository.findAfterCursor(afterId, pageable);

        boolean hasNextPage = students.getContent().size() > limit;
        List<com.example.controller.Student> mappedStudents = students
                .stream()
                .map(e -> { return new com.example.controller.Student(e.getId(), e.getFirstName());})
                .toList();

        if (hasNextPage) {
            mappedStudents = mappedStudents.subList(0, limit);  // Trim extra record
        }

        return new StudentPage(mappedStudents, hasNextPage, (int) students.getTotalElements());
    }

    public Student createStudent(CreateStudentRequest createStudentRequest) {
        Student student = new Student(createStudentRequest);

        Address address = new Address();
        address.setStreet(createStudentRequest.getStreet());
        address.setCity(createStudentRequest.getCity());

        address = addressRepository.save(address);

        student.setAddress(address);
        student = studentRepository.save(student);

        List<Subject> subjectsList = new ArrayList<Subject>();

        if (createStudentRequest.getSubjectsLearning() != null) {
            for (CreateSubjectRequest createSubjectRequest :
                    createStudentRequest.getSubjectsLearning()) {
                Subject subject = new Subject();
                subject.setSubjectName(createSubjectRequest.getSubjectName());
                subject.setMarksObtained(createSubjectRequest.getMarksObtained());
                subject.setStudent(student);

                subjectsList.add(subject);
            }

            subjectRepository.saveAll(subjectsList);

        }

        student.setLearningSubjects(subjectsList);

        return student;
    }

}
