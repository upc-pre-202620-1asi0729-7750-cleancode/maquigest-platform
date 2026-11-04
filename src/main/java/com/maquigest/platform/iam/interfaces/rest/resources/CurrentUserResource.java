package com.maquigest.platform.iam.interfaces.rest.resources;

import java.util.List;

/** Minimal principal information for validating a bearer token from Swagger. */
public record CurrentUserResource(String email, List<String> authorities) {
}
