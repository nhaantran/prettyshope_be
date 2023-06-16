package com.prettyshopbe.prettyshopbe.dto.user;

public class SignInResponseDto {
    private String lastName;
    private String firstName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private String status;
    private String token;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SignInResponseDto(String status, String token, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.token = token;
    }
}
