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

    // @GET
    // @Path("/allenrollable/{studentId}")
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getAllEnrollableCourses(
    // @PathParam("studentId") String courseId
    // ) {
    // try {
    // List<CourseEntity> courses = repository.findAll();

    // if (courses == null || courses.isEmpty()) {
    // return Response.status(Response.Status.NOT_FOUND).build();
    // }

    // return Response.ok(courses).build();

    // } catch (Exception e) {
    // e.printStackTrace();

    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    // .entity("{\"error\": \"" + e.getMessage() + "\"}")
    // .build();
    // }
    // }

    @PUT
    @Path("/update/{courseId}")
    @Secured
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
    @Secured
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

    @GET
    @Path("/{courseId}/prerequisites")
    public Response getPrerequisites(@PathParam("courseId") String courseId) {
        try {
            return Response.ok(repository.getPrerequisites(courseId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==========================================
    // INTER-SERVICE APIs (Used by Enrolment Service)
    // ==========================================

    @POST
    @Path("/{courseId}/reserve-seat")
    public Response reserveSeat(@PathParam("courseId") String courseId) {
        try {
            boolean reserved = repository.reserveSeat(courseId);
            if (reserved) {
                return Response.ok("{\"message\": \"Seat reserved successfully.\"}").build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Course is at maximum capacity.\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{courseId}/release-seat")
    public Response releaseSeat(@PathParam("courseId") String courseId) {
        try {
            repository.releaseSeat(courseId);
            return Response.ok("{\"message\": \"Seat released successfully.\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/code/{courseCode}")
    // @Secured(roles = {"ADMIN"}) // Optional: Restrict to inter-service / admin
    // calls
    public Response getCourseIdByCode(@PathParam("courseCode") String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Course code parameter is required.\"}")
                    .build();
        }

        try {
            CourseEntity course = repository.findByCourseCode(courseCode.trim());

            if (course == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Course not found with code: " + courseCode + "\"}")
                        .build();
            }

            // Return a simple JSON object expected by the Enrollment Service client
            String jsonResponse = "{\"courseId\": \"" + course.getCourseId() + "\"}";

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==========================================
    // FACULTY APIs
    // ==========================================

    // @POST
    // @Path("/{courseId}/change-requests")
    // // @Secured(roles = {"FACULTY"})
    // public Response submitChangeRequest(@PathParam("courseId") String courseId,
    // CourseChangeRequestEntity request) {
    // try {
    // request.setCourseId(courseId);
    // request.setStatus(RequestStatus.PENDING);
    // repository.saveChangeRequest(request);
    // return Response.status(Response.Status.CREATED).entity(request).build();
    // } catch (Exception e) {
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    // .entity("{\"error\":\"" + e.getMessage() + "\"}")
    // .build();
    // }
    // }

}