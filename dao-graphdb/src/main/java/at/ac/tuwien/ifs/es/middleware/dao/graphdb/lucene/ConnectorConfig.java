package at.ac.tuwien.ifs.es.middleware.dao.graphdb.lucene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration of the Lucene connector that shall be used by {@link Lucene}.
 *
 * @author Kevin Haller
 * @version 1.2
 * @see <a href="https://graphdb.ontotext.com/documentation/standard/lucene-graphdb-connector.html">GraphDB
 * Lucene connector documentation</a>
 * @since 1.2
 */
@Lazy
@Configuration
@JsonSerialize(as = ConnectorConfig.class)
public class ConnectorConfig {

  @JsonIgnore
  @Value("${graphdb.lucene.name:esm}")
  private String name;

  @Value("${graphdb.lucene.readonly:false}")
  private boolean readOnly;

  @Value("${graphdb.lucene.languages:}")
  private String languages;

  @Value("${graphdb.lucene.types:}")
  private String types;

  /**
   * Gets the name of the Lucene connector registered in GraphDB.
   *
   * @return the name of the registered Lucene connector.
   */
  public String getName() {
    return name;
  }

  /**
   * A read-only connector will index all existing data in the repository at creation time,
   * but,unlike non-read-only connectors, it will: <br/> (1) Not react to updates. Changes will not
   * be synced to the connector. <br/> (2) Not keep any extra structures (such as the internal
   * Lucene index for tracking updates to chains) <br/> The only way to index changes in data after
   * the connector has been created is to repair (or drop/recreate) the connector.
   *
   * @return {@code true}, if the connector is readonly, othetwise {@code false}.
   */
  @JsonProperty("readonly")
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * RDF data is often multilingual, but you can decide to map only some of the languages
   * represented in the literal values. This can be done by specifying a list of language ranges to
   * be matched to the language tags of literals according to RFC 4647.
   *
   * @return a {@link List} of languages to index, or an empty list, if all languages shall be
   * indexed.
   */
  @JsonProperty("languages")
  public List<String> getLanguages() {
    if (languages == null || languages.isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(languages.split(","));
  }

  /**
   * The RDF types of entities to sync are specified as a list of IRIs. At least one type IRI is
   * required. <br/> Use the pseudo-IRI {@code $any} to sync entities that have at least one RDF
   * type. <br/> Use the pseudo-IRI $untyped to sync entities regardless of whether they have any
   * RDF type.<br/>
   *
   * @return a {@link List} of types to index.
   */
  @JsonProperty("types")
  public List<String> getTypes() {
    if (types == null || languages.isEmpty()) {
      return Collections.singletonList("$untyped");
    }
    return Arrays.asList(types.split(","));
  }

  /**
   * The fields define exactly what parts of each entity will be synchronized as well as the
   * specific details on the connector side. The field is the smallest synchronization unit and it
   * maps a property chain from GraphDB to a field in Lucene. The fields are specified as a list of
   * field objects. At least one field object is required. Each field object has further keys that
   * specify details.
   *
   * @return the default {@link Field} for indexing literals of entities.
   */
  @JsonProperty("fields")
  public List<Field> getFields() {
    return Collections.singletonList(new Field());
  }

  @JsonSerialize(as = Field.class)
  private static class Field {

    @JsonProperty("fieldName")
    public String getFieldName() {
      return "fts";
    }

    @JsonProperty("propertyChain")
    public List<String> getPropertyChain() {
      return Collections.singletonList("$literal");
    }

    @JsonProperty("facet")
    public boolean shallFacetsBeEnabled() {
      return false;
    }
  }
}