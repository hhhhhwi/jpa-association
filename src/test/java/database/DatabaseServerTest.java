package database;

import fixtures.EntityFixtures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import persistence.DatabaseTest;
import persistence.entity.attribute.EntityAttribute;
import persistence.mapper.TestEntityRowMapper;
import persistence.sql.dml.builder.InsertQueryBuilder;
import persistence.sql.infra.H2SqlConverter;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Nested
@DisplayName("DatabaseServer 클래스의")
public class DatabaseServerTest extends DatabaseTest {
    @Nested
    @DisplayName("executeQuery 메소드는")
    class executeQuery {
        @Nested
        @DisplayName("유효한 데이터가 주어지면")
        class withValidArgs {
            @Test
            @DisplayName("예외를 던지지않고 종료한다.")
            void doseNotThrowException() {
                setUpFixtureTable(EntityFixtures.SampleOneWithValidAnnotation.class, new H2SqlConverter());

                InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder();

                EntityAttribute entityAttribute = EntityAttribute.of(EntityFixtures.SampleOneWithValidAnnotation.class,new HashSet<>());

                EntityFixtures.SampleOneWithValidAnnotation entityOne =
                        new EntityFixtures.SampleOneWithValidAnnotation("민준", 29);
                EntityFixtures.SampleOneWithValidAnnotation entityTwo =
                        new EntityFixtures.SampleOneWithValidAnnotation("민준", 29);
                EntityFixtures.SampleOneWithValidAnnotation entityThree =
                        new EntityFixtures.SampleOneWithValidAnnotation("민준", 29);

                String insertDMLOne = insertQueryBuilder.prepareStatement(entityAttribute, entityOne);
                String insertDMLTwo = insertQueryBuilder.prepareStatement(entityAttribute, entityTwo);
                String insertDMLThree = insertQueryBuilder.prepareStatement(entityAttribute, entityThree);

                Assertions.assertAll(
                        () -> Assertions.assertDoesNotThrow(() -> jdbcTemplate.execute(insertDMLOne)),
                        () -> Assertions.assertDoesNotThrow(() -> jdbcTemplate.execute(insertDMLTwo)),
                        () -> Assertions.assertDoesNotThrow(() -> jdbcTemplate.execute(insertDMLThree))
                );

                List<EntityFixtures.SampleOneWithValidAnnotation> entities =
                        jdbcTemplate.queryForObject("SELECT * FROM ENTITY_NAME;", new TestEntityRowMapper());

                assertThat(entities.size()).isEqualTo(3);
            }
        }
    }
}
