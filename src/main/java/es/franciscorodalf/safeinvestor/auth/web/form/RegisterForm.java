package es.franciscorodalf.safeinvestor.auth.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 1, max = 100)
    private String nombre;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public void setEmail(String email) { this.email = email; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPassword(String password) { this.password = password; }
}
