package com.example.aadpractica;

public class Contacto {

    private String nombre;
    private long id;
    private long telefono;

    public Contacto(String nombre, int id, long telefono) {
        this.nombre = nombre;
        this.id = id;
        this.telefono = telefono;
    }

    public Contacto() {
    }

    public String getNombre() {
        return nombre;
    }

    public long getId() {
        return id;
    }

    public long getTelefono() {
        return telefono;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTelefono(long telefono) {
        this.telefono = telefono;
    }
}
