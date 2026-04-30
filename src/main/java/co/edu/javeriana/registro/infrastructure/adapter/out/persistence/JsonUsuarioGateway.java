package co.edu.javeriana.registro.infrastructure.adapter.out.persistence;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class JsonUsuarioGateway implements UsuarioGateway {

    private final File archivoJson;
    private final ObjectMapper mapper;

    public JsonUsuarioGateway(String rutaArchivo) {
        this.archivoJson = new File(rutaArchivo);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        
        if (!archivoJson.exists()) {
            try {
                archivoJson.createNewFile();
                escribirUsuarios(new ArrayList<>());
            } catch (IOException e) {
                throw new RuntimeException("No se pudo inicializar el archivo JSON", e);
            }
        }
    }

    private List<UsuarioDTO> leerUsuarios() {
        try {
            if (archivoJson.length() == 0) return new ArrayList<>();
            return mapper.readValue(archivoJson, new TypeReference<List<UsuarioDTO>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo usuarios del JSON", e);
        }
    }

    private void escribirUsuarios(List<UsuarioDTO> usuarios) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(archivoJson, usuarios);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo usuarios al JSON", e);
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        List<UsuarioDTO> dtos = leerUsuarios();
        
        // Remover si ya existe (update)
        dtos.removeIf(dto -> dto.email.equals(usuario.getEmail()));
        
        // Agregar el nuevo/actualizado
        dtos.add(UsuarioDTO.fromDomain(usuario));
        
        escribirUsuarios(dtos);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return leerUsuarios().stream()
                .filter(dto -> dto.email.equals(email))
                .findFirst()
                .map(UsuarioDTO::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return leerUsuarios().stream()
                .filter(dto -> dto.id.equals(id))
                .findFirst()
                .map(UsuarioDTO::toDomain);
    }

    @Override
    public List<Usuario> buscarCuentasInactivasExpiradas(LocalDateTime fechaCorte) {
        return leerUsuarios().stream()
                .map(UsuarioDTO::toDomain)
                .filter(u -> u.esElegibleParaLimpieza(fechaCorte))
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Usuario usuario) {
        List<UsuarioDTO> dtos = leerUsuarios();
        dtos.removeIf(dto -> dto.email.equals(usuario.getEmail()));
        escribirUsuarios(dtos);
    }

    // DTO Privado para serialización sin acoplar el Dominio
    private static class UsuarioDTO {
        public String id;
        public String nombre;
        public String email;
        public EstadoUsuario estado;
        public String codigoValidacion;
        public LocalDateTime fechaExpiracionCodigo;
        public LocalDateTime fechaRegistro;
        public String passwordHash;
        public int intentosFallidos;
        public LocalDateTime fechaBloqueo;

        public UsuarioDTO() {} // Jackson

        public static UsuarioDTO fromDomain(Usuario u) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.id = u.getId();
            dto.nombre = u.getNombre();
            dto.email = u.getEmail();
            dto.estado = u.getEstado();
            dto.fechaRegistro = u.getFechaRegistro();
            dto.passwordHash = u.getPasswordHash();
            dto.intentosFallidos = u.getIntentosFallidos();
            dto.fechaBloqueo = u.getFechaBloqueo();
            if (u.getCodigoValidacionActivo() != null) {
                dto.codigoValidacion = u.getCodigoValidacionActivo().getCodigo();
                dto.fechaExpiracionCodigo = u.getCodigoValidacionActivo().getFechaExpiracion();
            }
            return dto;
        }

        public Usuario toDomain() {
            Usuario u = new Usuario(this.id, this.nombre, this.email);
            // Usamos reflection o setters package-private para reconstruir el estado.
            // Dado que Usuario no tiene setters, podemos modificar el constructor o inyectar via reflection.
            // Para mantener el diseño limpio del Dominio, en este caso simplificado reconstruimos y asignamos.
            try {
                java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
                estadoField.setAccessible(true);
                estadoField.set(u, this.estado);

                java.lang.reflect.Field fechaRegistroField = Usuario.class.getDeclaredField("fechaRegistro");
                fechaRegistroField.setAccessible(true);
                fechaRegistroField.set(u, this.fechaRegistro);

                java.lang.reflect.Field passwordHashField = Usuario.class.getDeclaredField("passwordHash");
                passwordHashField.setAccessible(true);
                passwordHashField.set(u, this.passwordHash);

                java.lang.reflect.Field intentosFallidosField = Usuario.class.getDeclaredField("intentosFallidos");
                intentosFallidosField.setAccessible(true);
                intentosFallidosField.set(u, this.intentosFallidos);

                java.lang.reflect.Field fechaBloqueoField = Usuario.class.getDeclaredField("fechaBloqueo");
                fechaBloqueoField.setAccessible(true);
                fechaBloqueoField.set(u, this.fechaBloqueo);

                if (this.codigoValidacion != null && this.fechaExpiracionCodigo != null) {
                    CodigoValidacion cv = new CodigoValidacion(this.codigoValidacion, this.fechaExpiracionCodigo);
                    java.lang.reflect.Field cvField = Usuario.class.getDeclaredField("codigoValidacionActivo");
                    cvField.setAccessible(true);
                    cvField.set(u, cv);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error reconstruyendo Usuario desde DB", e);
            }
            return u;
        }
    }
}
