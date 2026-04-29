package co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SqliteUsuarioGateway implements UsuarioGateway {

    private final EntityManagerFactory emf;

    public SqliteUsuarioGateway(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void guardar(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            UsuarioJpaEntity entity = toEntity(usuario);
            em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error guardando usuario en SQLite", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            UsuarioJpaEntity entity = em.createQuery("SELECT u FROM UsuarioJpaEntity u WHERE u.email = :email", UsuarioJpaEntity.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Usuario> buscarCuentasInactivasExpiradas(LocalDateTime fechaCorte) {
        EntityManager em = emf.createEntityManager();
        try {
            // Usuarios NO_VERIFICADO que han expirado o CADUCADOS
            List<UsuarioJpaEntity> entities = em.createQuery(
                    "SELECT u FROM UsuarioJpaEntity u", UsuarioJpaEntity.class)
                    .getResultList();
                    
            return entities.stream()
                    .map(this::toDomain)
                    .filter(u -> u.esElegibleParaLimpieza(fechaCorte))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            UsuarioJpaEntity entity = em.find(UsuarioJpaEntity.class, usuario.getId());
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Error eliminando usuario en SQLite", e);
        } finally {
            em.close();
        }
    }

    // Mappers
    private UsuarioJpaEntity toEntity(Usuario domain) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setEmail(domain.getEmail());
        entity.setEstado(domain.getEstado().name());
        entity.setFechaRegistro(domain.getFechaRegistro());
        
        if (domain.getCodigoValidacionActivo() != null) {
            entity.setCodigoValidacion(domain.getCodigoValidacionActivo().getCodigo());
            entity.setFechaExpiracionCodigo(domain.getCodigoValidacionActivo().getFechaExpiracion());
        }
        return entity;
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {
        Usuario u = new Usuario(entity.getId(), entity.getNombre(), entity.getEmail());
        try {
            java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
            estadoField.setAccessible(true);
            estadoField.set(u, EstadoUsuario.valueOf(entity.getEstado()));

            java.lang.reflect.Field fechaRegistroField = Usuario.class.getDeclaredField("fechaRegistro");
            fechaRegistroField.setAccessible(true);
            fechaRegistroField.set(u, entity.getFechaRegistro());

            if (entity.getCodigoValidacion() != null && entity.getFechaExpiracionCodigo() != null) {
                CodigoValidacion cv = new CodigoValidacion(entity.getCodigoValidacion(), entity.getFechaExpiracionCodigo());
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
