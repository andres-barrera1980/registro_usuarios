package co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String estado;

    private String codigoValidacion;

    private LocalDateTime fechaExpiracionCodigo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    private String passwordHash;

    @Column(nullable = false)
    private int intentosFallidos = 0;

    private LocalDateTime fechaBloqueo;

    public UsuarioJpaEntity() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCodigoValidacion() { return codigoValidacion; }
    public void setCodigoValidacion(String codigoValidacion) { this.codigoValidacion = codigoValidacion; }

    public LocalDateTime getFechaExpiracionCodigo() { return fechaExpiracionCodigo; }
    public void setFechaExpiracionCodigo(LocalDateTime fechaExpiracionCodigo) { this.fechaExpiracionCodigo = fechaExpiracionCodigo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(LocalDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
}
