package com.nexusenroll.course.db;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;

import java.util.List;

import com.nexusenroll.course.db.entities.course.CourseEntity;
import com.nexusenroll.course.db.entities.prerequisite.CoursePrerequisiteEntity;

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

    // ==========================================
    // CAPACITY MANAGEMENT (SAGA PATTERN)
    // ==========================================

    /**
     * Reserves a seat using pessimistic locking to prevent race conditions.
     * Returns true if successful, false if the course is full.
     */
    public boolean reserveSeat(String courseId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Lock the row so no other transaction can read/write it until we commit
            CourseEntity course = em.find(CourseEntity.class, courseId, LockModeType.PESSIMISTIC_WRITE);

            if (course == null || course.getAvailableSeats() <= 0) {
                tx.rollback();
                return false;
            }

            course.setAvailableSeats(course.getAvailableSeats() - 1);
            em.merge(course);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    /**
     * Releases a seat (used when a student drops, or as a Saga compensating
     * transaction).
     */
    public void releaseSeat(String courseId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CourseEntity course = em.find(CourseEntity.class, courseId, LockModeType.PESSIMISTIC_WRITE);

            if (course != null && course.getAvailableSeats() < course.getTotalCapacity()) {
                course.setAvailableSeats(course.getAvailableSeats() + 1);
                em.merge(course);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        }
    }

    // ==========================================
    // PREREQUISITES & REPORTS
    // ==========================================

    public List<CoursePrerequisiteEntity> getPrerequisites(String courseId) {
        return em.createQuery(
                "SELECT p FROM CoursePrerequisiteEntity p WHERE p.courseId = :courseId",
                CoursePrerequisiteEntity.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    /**
     * Finds a CourseEntity by its course code (e.g., "SCS2303").
     * Returns null if no course is found.
     */
    public CourseEntity findByCourseCode(String courseCode) {
        try {
            return em.createQuery(
                    "SELECT c FROM CourseEntity c WHERE c.courseCode = :courseCode",
                    CourseEntity.class)
                    .setParameter("courseCode", courseCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Course not found
        }
    }
}
