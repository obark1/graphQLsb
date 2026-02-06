package com.example.repository;

import com.example.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
	
	List<Student> findByFirstName(String firstname);
	
	Student findByLastNameAndFirstName (String lastName, String firstName);
	
	List<Student> findByFirstNameOrLastName (String firstName, String lastName);
	
	List<Student> findByFirstNameIn (List<String> firstNames);

	List<Student> findByAddressCity(String city);

	@Query("From Student where firstName = :firstname and lastName = :lastName")
	Student getByLastNameAndFirstName (String lastName, @Param("firstname") String firstName);
	
	@Modifying
	@Transactional
	@Query("Update Student set firstName = :firstName where id = :id")
	Integer updateFirstName (Long id, String firstName);
	
	@Modifying
	@Transactional
	@Query("Delete From Student where firstName = :firstName")
	Integer deleteByFirstName (String firstName);
	
	List<Student> findByAddressCityIgnoreCaseAndLearningSubjectsSubjectNameIgnoreCase(String city,  String subjectName);

	@Query("SELECT DISTINCT s FROM Student s " +
			"JOIN FETCH s.learningSubjects ls " +
			"WHERE LOWER(s.address.city) = LOWER(:city) " +
			"AND LOWER(ls.subjectName) = LOWER(:subjectName)")
	List<Student> findByCityAndSubjectName(@Param("city") String city,
										   @Param("subjectName") String subjectName);
	
	@Query("From Student where address.city = :city")
	List<Student> getByAddressCity (String city);

	@Query(value="Select * FROM student order by id limit ?2 offset ?1", nativeQuery = true)
	List<Student> findAllWithOffsetAndLimit(int offset, int limit);

	@Query("SELECT s FROM Student s WHERE s.id > :afterId ORDER BY s.id")
	Page<Student> findAfterCursor(@Param("afterId") Long afterId, Pageable pageable);
}
