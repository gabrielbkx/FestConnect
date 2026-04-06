package com.gabriel.party.model.usuario.enums;


public enum Role {
    ROLE_ADMINISTRADOR("role_administrador"),
    ROLE_PRESTADOR("role_prestador"),
    ROLE_CLIENTE("role_clinete");

   private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
