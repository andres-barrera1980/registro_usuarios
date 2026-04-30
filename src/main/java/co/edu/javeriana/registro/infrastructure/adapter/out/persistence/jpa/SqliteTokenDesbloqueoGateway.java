package co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa;

import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class SqliteTokenDesbloqueoGateway implements TokenDesbloqueoGateway {

    private final EntityManagerFactory emf;

    public SqliteTokenDesbloqueoGateway(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void guardar(TokenDesbloqueo token) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            TokenDesbloqueoJpaEntity entity = toEntity(token);
            em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error guardando token de desbloqueo", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<TokenDesbloqueo> buscarPorToken(String tokenStr) {
        EntityManager em = emf.createEntityManager();
        try {
            TokenDesbloqueoJpaEntity entity = em.find(TokenDesbloqueoJpaEntity.class, tokenStr);
            return entity != null ? Optional.of(toDomain(entity)) : Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public void revocarTokensDeUsuario(String userId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            List<TokenDesbloqueoJpaEntity> tokens = em.createQuery(
                    "SELECT t FROM TokenDesbloqueoJpaEntity t WHERE t.userId = :userId AND t.revocado = false",
                    TokenDesbloqueoJpaEntity.class)
                    .setParameter("userId", userId)
                    .getResultList();

            for (TokenDesbloqueoJpaEntity token : tokens) {
                token.setRevocado(true);
                em.merge(token);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error revocando tokens de desbloqueo", e);
        } finally {
            em.close();
        }
    @Override
    public Optional<TokenDesbloqueo> buscarUltimoTokenDeUsuario(String userId) {
        EntityManager em = emf.createEntityManager();
        try {
            TokenDesbloqueoJpaEntity entity = em.createQuery(
                    "SELECT t FROM TokenDesbloqueoJpaEntity t WHERE t.userId = :userId AND t.revocado = false AND t.usado = false ORDER BY t.fechaCreacion DESC",
                    TokenDesbloqueoJpaEntity.class)
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .getSingleResult();
            return Optional.of(toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    // Mappers
    private TokenDesbloqueoJpaEntity toEntity(TokenDesbloqueo domain) {
        TokenDesbloqueoJpaEntity entity = new TokenDesbloqueoJpaEntity();
        entity.setToken(domain.getToken());
        entity.setUserId(domain.getUserId());
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaExpiracion(domain.getFechaExpiracion());
        entity.setUsado(domain.isUsado());
        entity.setRevocado(domain.isRevocado());
        return entity;
    }

    private TokenDesbloqueo toDomain(TokenDesbloqueoJpaEntity entity) {
        return new TokenDesbloqueo(
                entity.getToken(),
                entity.getUserId(),
                entity.getFechaCreacion(),
                entity.getFechaExpiracion(),
                entity.isUsado(),
                entity.isRevocado()
        );
    }
}
