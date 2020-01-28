package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation shall be used for {@link KGDAO}s and indicates on which other {@link KGDAO}s the
 * annotated {@link KGDAO} depends on.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependsOn {

  /**
   * If this DAO depends on the {@link KGSparqlDAO}, then {@code true} is returned, otherwise {@code
   * false}. The default value is {@code false}.
   *
   * @return {@code true}, if this DAO depends on the {@link KGSparqlDAO}, otherwise {@code false}.
   */
  boolean sparql() default false;

  /**
   * If this DAO depends on the {@link KGGremlinDAO}, then {@code true} is returned, otherwise
   * {@code false}. The default value is {@code false}.
   *
   * @return {@code true}, if this DAO depends on the {@link KGGremlinDAO}, otherwise {@code false}.
   */
  boolean gremlin() default false;

  /**
   * If this DAO depends on the {@link KGFullTextSearchDAO}, then {@code true} is returned,
   * otherwise {@code false}. The default value is {@code false}.
   *
   * @return {@code true}, if this DAO depends on the {@link KGFullTextSearchDAO}, otherwise {@code
   * false}.
   */
  boolean fulltextSearch() default false;
}
