package com.nexusenroll.course.db;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

import com.nexusenroll.course.db.entities.course.CourseEntity;

@Singleton
public class CourseRepository {

    @Inject
    private EntityManager em;

    public void saveCourse(CourseEntity course) {
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(course);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public CourseEntity findByCourseId(String courseId) {
        return em.find(CourseEntity.class, courseId);
    }

    public List<CourseEntity> findAll() {
        return em.createQuery(
                "SELECT c FROM CourseEntity c",
                CourseEntity.class).getResultList();
    }

    public CourseEntity updateCourse(String courseId, CourseEntity updatedCourse) {

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            CourseEntity existingCourse = em.find(CourseEntity.class, courseId);

            if (existingCourse == null) {
                transaction.rollback();
                return null;
            }

            existingCourse.setCourseCode(updatedCourse.getCourseCode());
            existingCourse.setTitle(updatedCourse.getTitle());
            existingCourse.setDescription(updatedCourse.getDescription());
            existingCourse.setDepartment(updatedCourse.getDepartment());
            existingCourse.setTotalCapacity(updatedCourse.getTotalCapacity());
            existingCourse.setScheduleDay(updatedCourse.getScheduleDay());
            existingCourse.setStartTime(updatedCourse.getStartTime());
            existingCourse.setEndTime(updatedCourse.getEndTime());
            existingCourse.setInstructorId(updatedCourse.getInstructorId());

            /*
             * No em.persist() here.
             *
             * existingCourse was obtained using em.find(), so it is
             * already a managed entity. JPA will automatically detect
             * the changes when the transaction commits.
             */

            transaction.commit();

            return existingCourse;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }
}