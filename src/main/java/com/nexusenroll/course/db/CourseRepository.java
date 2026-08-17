package com.nexusenroll.course.db;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import java.util.List;

import com.nexusenroll.course.db.entities.course.CourseEntity;

@Singleton
public class CourseRepository {

    @Inject
    private EntityManager em;

    public void saveCourse(CourseEntity course) {
        em.getTransaction().begin();
        em.persist(course);
        em.getTransaction().commit();
    }

    public CourseEntity findByCourseId(String courseId) {
        return em.find(CourseEntity.class, courseId);
    }

    public List<CourseEntity> findAll() {
        return em.createQuery(
                "SELECT c FROM CourseEntity c",
                CourseEntity.class).getResultList();
    }
}