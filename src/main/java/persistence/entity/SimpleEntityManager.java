package persistence.entity;

import jakarta.persistence.Id;
import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class SimpleEntityManager implements EntityManager{
    private final EntityPersister entityPersister;

    private final EntityLoader entityLoader;

    private final PersistenceContext persistenceContext;

    public SimpleEntityManager(JdbcTemplate jdbcTemplate, Class<?> clazz, PersistenceContext persistenceContext) {
        DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        this.entityPersister = new EntityPersister(jdbcTemplate, clazz, dmlQueryBuilder);
        this.entityLoader = new EntityLoader(jdbcTemplate, clazz, dmlQueryBuilder);
        this.persistenceContext = persistenceContext;
    }

    @Override
    public <T> T find(Class<T> clazz, Long id) {
        Object entity = persistenceContext.getEntity(clazz, id);

        if(Objects.isNull(entity)) {
            entity = entityLoader.find(clazz, id);
            persistenceContext.addEntity(id, entity);
        }

        return clazz.cast(entity);
    }

    @Override
    public void persist(Object entity) {
        Object id = entityPersister.insert(entity);

        Field idField = Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Id 값이 정의되지 않은 엔티티입니다."));

        try {
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        persistenceContext.addEntity(id, entity);
    }

    @Override
    public void remove(Object entity) {
        persistenceContext.removeEntity(entityPersister.getIdValue(entity), entity);
        entityPersister.delete(entity);
    }

    @Override
    public void merge(Object entity) {
        Snapshot snapshot = persistenceContext.getCachedDatabaseSnapshot(entityPersister.getIdValue(entity), entity);

        if(snapshot == null) {
            persist(entity);
            return;
        }

        Field[] fields = snapshot.getChangedColumns(entity);

        if(fields.length == 0) {
            return;
        }

        persistenceContext.addEntity(entityPersister.getIdValue(entity), entity);
        entityPersister.update(fields, entity);
    }
}
