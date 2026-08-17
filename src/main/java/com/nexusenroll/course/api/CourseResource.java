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
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCourses() {
        try {
            List<CourseEntity> courses = repository.findAll();

            if (courses == null || courses.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(courses).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/update/{courseId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCourse(
            @PathParam("courseId") String courseId,
            CourseEntity course) {

        try {
            CourseEntity updatedCourse = repository.updateCourse(courseId, course);

            if (updatedCourse == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Course not found\"}")
                        .build();
            }

            return Response.ok(updatedCourse).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Administrator Control: Manage Course Offerings[cite: 1]
    @POST
    @Path("/create")
    // @Secured(roles = { "ADMIN" })
    public Response createCourse(CourseEntity newCourse) {
        newCourse.setAvailableSeats(newCourse.getTotalCapacity());
        repository.saveCourse(newCourse);
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