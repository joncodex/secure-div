package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseResultRepo extends JpaRepository<CourseResult, Long> {
    List<CourseResult> findCourseResultsByCourseCodeIn(Collection<String> courseCodes);

    void deleteByCourseCode(String courseCode);

    Optional<CourseResult> findCourseResultByCourseCode(String courseCode);
}
