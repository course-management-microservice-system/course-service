package com.nexusenroll.course.db.entities.prerequisite;

import jakarta.persistence.*;

@Entity
@Table(name = "course_prerequisites", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "course_id", "prerequisite_course_id" })
})
public class CoursePrerequisiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The target course that the student wants to take (e.g., "CS202")
    @Column(name = "course_id", nullable = false, length = 36)
    private String courseId;

    // The course that must be completed first (e.g., "CS101")
    @Column(name = "prerequisite_course_id", nullable = false, length = 36)
    private String prerequisiteCourseId;

    // Required by JPA
    public CoursePrerequisiteEntity() {
    }

    public CoursePrerequisiteEntity(String courseId, String prerequisiteCourseId) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    public void setPrerequisiteCourseId(String prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }
}