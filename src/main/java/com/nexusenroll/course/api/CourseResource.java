package com.nexusenroll.course.api;

import com.nexusenroll.course.db.CourseRepository;
import com.nexusenroll.course.db.entities.course.CourseEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import com.nexusenroll.course.config.Secured;

@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class CourseResource {

    @Inject
    private CourseRepository repository;

    @GET
    public Response getAllCourses() {
        List<CourseEntity> courses = repository.findAll();
        return Response.ok(courses).build();
    }

    // Administrator Control: Manage Course Offerings[cite: 1]
    @POST
    // @Secured(roles = {"ADMIN"}) <-- Ensure only admins can create courses
    public Response createCourse(CourseEntity newCourse) {
        // repository.save(newCourse);
        return Response.status(Response.Status.CREATED).entity(newCourse).build();
    }

    // API for Inter-Service Communication (Used by Student Service for validation)
    @GET
    @Path("/{courseId}/capacity")
    public Response getCourseCapacity(@PathParam("courseId") String courseId) {
        CourseEntity course = repository.findByCourseId(courseId);
        if (course == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok("{\"availableSeats\":" + course.getAvailableSeats() + "}").build();
    }
}