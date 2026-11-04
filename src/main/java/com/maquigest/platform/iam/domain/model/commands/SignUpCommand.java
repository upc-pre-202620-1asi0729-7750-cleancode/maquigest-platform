package com.maquigest.platform.iam.domain.model.commands;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;

/** Domain command for signing up a company user in MaquiGest. */
public record SignUpCommand(String firstName, String lastName, String email,
                            String password, String companyName, Roles role) {
}
