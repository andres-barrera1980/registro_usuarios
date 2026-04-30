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

    private static class UsuarioDTO {
        public String id;
        public String nombre;
        public String email;
        public String passwordHash;
        public EstadoUsuario estado;
        public String codigoValidacion;
        public LocalDateTime fechaExpiracionCodigo;
        public LocalDateTime fechaRegistro;

        public UsuarioDTO() {} // Jackson

        public static UsuarioDTO fromDomain(Usuario u) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.id = u.getId();
            dto.nombre = u.getNombre();
            dto.email = u.getEmail();
            dto.passwordHash = u.getPasswordHash();
            dto.estado = u.getEstado();
            dto.fechaRegistro = u.getFechaRegistro();
            if (u.getCodigoValidacionActivo() != null) {
                dto.codigoValidacion = u.getCodigoValidacionActivo().getCodigo();
                dto.fechaExpiracionCodigo = u.getCodigoValidacionActivo().getFechaExpiracion();
            }
            return dto;
        }

        public Usuario toDomain() {
            Usuario u = new Usuario(this.id, this.nombre, this.email, this.passwordHash,
                                   this.estado, this.fechaRegistro);
            if (this.codigoValidacion != null && this.fechaExpiracionCodigo != null) {
                CodigoValidacion cv = new CodigoValidacion(this.codigoValidacion, this.fechaExpiracionCodigo);
                u.asignarNuevoCodigo(cv);
            }
            return u;
        }
    }
}
