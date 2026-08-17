package com.nexusenroll.course.config;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

public class CorsFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(AuthenticationFilter.class);
        return true;
    }
}