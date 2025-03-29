package es.franciscorodalf.saveinvestor.backend.model;

import java.util.List;
import java.util.Objects;

public class usuario {
    private String nombre;
    private String gmail;
    private String contrasenia;
    private List<tarea> tarea;

    public usuario() {
    }

    public usuario(String nombre, String usuario, String gmail, String contrasenia, List<tarea> tarea) {
        this.nombre = nombre;
        this.gmail = gmail;
        this.contrasenia = contrasenia;
        this.tarea = tarea;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGmail() {
        return this.gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getContrasenia() {
        return this.contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public List<tarea> getTarea() {
        return this.tarea;
    }

    public void setTarea(List<tarea> tarea) {
        this.tarea = tarea;
    }

    public usuario nombre(String nombre) {
        setNombre(nombre);
        return this;
    }

    public usuario gmail(String gmail) {
        setGmail(gmail);
        return this;
    }

    public usuario contrasenia(String contrasenia) {
        setContrasenia(contrasenia);
        return this;
    }

    public usuario tarea(List<tarea> tarea) {
        setTarea(tarea);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof usuario)) {
            return false;
        }
        usuario usuario = (usuario) o;
        return Objects.equals(gmail, usuario.gmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gmail);
    }

    @Override
    public String toString() {
        return getNombre() + ", " + getGmail() + ", " + getContrasenia() + ", " + getTarea();
    }

}
