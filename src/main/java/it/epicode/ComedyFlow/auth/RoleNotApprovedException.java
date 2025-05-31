package it.epicode.ComedyFlow.auth;

public class RoleNotApprovedException extends RuntimeException {
    public RoleNotApprovedException(String message) {
        super(message);
    }
}
