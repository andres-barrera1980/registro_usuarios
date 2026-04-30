package co.edu.javeriana.registro.infrastructure.adapter.out.persistence;

import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonTokenDesbloqueoGateway implements TokenDesbloqueoGateway {

    private final File archivoJson;
    private final ObjectMapper mapper;

    public JsonTokenDesbloqueoGateway(String rutaArchivo) {
        this.archivoJson = new File(rutaArchivo);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        if (!archivoJson.exists()) {
            try {
                archivoJson.createNewFile();
                escribirTokens(new ArrayList<>());
            } catch (IOException e) {
                throw new RuntimeException("No se pudo inicializar el archivo JSON de tokens", e);
            }
        }
    }

    private List<TokenDTO> leerTokens() {
        try {
            if (archivoJson.length() == 0) return new ArrayList<>();
            return mapper.readValue(archivoJson, new TypeReference<List<TokenDTO>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo tokens del JSON", e);
        }
    }

    private void escribirTokens(List<TokenDTO> tokens) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(archivoJson, tokens);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo tokens al JSON", e);
        }
    }

    @Override
    public void guardar(TokenDesbloqueo token) {
        List<TokenDTO> dtos = leerTokens();
        dtos.removeIf(dto -> dto.token.equals(token.getToken()));
        dtos.add(TokenDTO.fromDomain(token));
        escribirTokens(dtos);
    }

    @Override
    public Optional<TokenDesbloqueo> buscarPorToken(String tokenStr) {
        return leerTokens().stream()
                .filter(dto -> dto.token.equals(tokenStr))
                .findFirst()
                .map(TokenDTO::toDomain);
    }

    @Override
    public void revocarTokensDeUsuario(String userId) {
        List<TokenDTO> dtos = leerTokens();
        for (TokenDTO dto : dtos) {
            if (dto.userId.equals(userId) && !dto.revocado) {
                dto.revocado = true;
            }
        }
        escribirTokens(dtos);
    }
    @Override
    public Optional<TokenDesbloqueo> buscarUltimoTokenDeUsuario(String userId) {
        return leerTokens().stream()
                .filter(dto -> dto.userId.equals(userId) && !dto.revocado && !dto.usado)
                .sorted((a, b) -> b.fechaCreacion.compareTo(a.fechaCreacion))
                .findFirst()
                .map(TokenDTO::toDomain);
    }
    private static class TokenDTO {
        public String token;
        public String userId;
        public LocalDateTime fechaCreacion;
        public LocalDateTime fechaExpiracion;
        public boolean usado;
        public boolean revocado;

        public TokenDTO() {}

        public static TokenDTO fromDomain(TokenDesbloqueo t) {
            TokenDTO dto = new TokenDTO();
            dto.token = t.getToken();
            dto.userId = t.getUserId();
            dto.fechaCreacion = t.getFechaCreacion();
            dto.fechaExpiracion = t.getFechaExpiracion();
            dto.usado = t.isUsado();
            dto.revocado = t.isRevocado();
            return dto;
        }

        public TokenDesbloqueo toDomain() {
            return new TokenDesbloqueo(token, userId, fechaCreacion, fechaExpiracion, usado, revocado);
        }
    }
}
