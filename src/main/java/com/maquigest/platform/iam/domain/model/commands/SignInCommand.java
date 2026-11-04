package com.maquigest.platform.iam.domain.model.commands;

/** CQRS sign-in command using the email used in MaquiGest WebApp. */
public record SignInCommand(String email, String password) {
}
