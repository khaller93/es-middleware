package at.ac.tuwien.ifs.es.middleware.dao.graphdb;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Instances of this class contain the configuration for a {@link GraphDbLucene} full-text-search
 * index.
 *
 * @author Kevin Haller
 * @version 1.0
 * @see <a href="http://graphdb.ontotext.com/free/full-text-search.html">Ontotext GraphDB FTS</a>
 * @since 1.0
 */
@Lazy
@Configuration
public class GraphDbLuceneConfig {

  public static final String GRAPHDB_LUCENE_NS = "http://www.ontotext.com/owlim/lucene#";

  private static final String TRIPLE_CONFIG_TEMPLATE = "${exclude}\n"
      + "${index}\n"
      + "${exclude-entities}\n"
      + "${exclude-predicates}\n"
      + "${include}\n"
      + "${include-entities}\n"
      + "${include-predicates}\n"
      + "${languages}\n"
      + "${molecule-size}\n"
      + "${use-rdfrank}\n"
      + "${analyser}\n"
      + "${scorer}";

  @Value("${graphdb.lucene.name:esm}")
  private String name;

  @Value("${graphdb.lucene.initialize:true}")
  private boolean initialize;

  private List<String> indexing = Collections.singletonList("uris");

  @Value("${graphdb.lucene.exclude:#{null}}")
  private String excludePattern;

  @Value("${graphdb.lucene.exclude.entities:#{null}}")
  private List<String> excludedEntities;

  @Value("${graphdb.lucene.exclude.predicates:#{null}}")
  private List<String> excludedPredicates;

  @Value("${graphdb.lucene.include:literals}")
  private List<String> includedNodes;

  @Value("${graphdb.lucene.include.entities:#{null}}")
  private List<String> includedEntities;

  @Value("${graphdb.lucene.include.predicates:#{null}}")
  private List<String> includedPredicates;

  @Value("${graphdb.lucene.molecule.size:1}")
  private Integer moleculeSize;

  @Value("${graphdb.lucene.languages:#{null}}")
  private List<String> languages;

  @Value("${graphdb.lucene.rdfrank.use:no}")
  private String useRDFRank;

  @Value("${graphdb.lucene.analyser:#{null}}")
  private String analyser;

  @Value("${graphdb.lucene.scorer:#{null}}")
  private String scorer;

  /**
   * Gets the configuration triples for {@link GraphDbLucene}. Those list of triple can be inserted
   * into a GraphDB instance to configure the Lucene index.
   *
   * @return the configuration triples for {@link GraphDbLucene}.
   */
  public String getConfigTriples() {
    return new StringSubstitutor(ImmutableMap.<String, String>builder()
        .put("index", indexing == null || indexing.isEmpty() ? ""
            : String.format("luc:index luc:setParam \"%s\".",
                indexing.stream().collect(Collectors.joining(","))))
        .put("exclude", excludePattern == null ? ""
            : String.format("luc:exclude luc:setParam \"%s\".", excludePattern))
        .put("exclude-entities",
            excludedEntities == null || excludedEntities.isEmpty() ? ""
                : String.format("luc:excludeEntities luc:setParam \"%s\".",
                    excludedEntities.stream().collect(
                        Collectors.joining(", "))))
        .put("exclude-predicates", excludedPredicates == null || excludedPredicates.isEmpty() ? "" :
            String.format("luc:excludePredicates luc:setParam \"%s\".",
                excludedPredicates.stream().collect(Collectors.joining(" "))))
        .put("include", includedNodes == null || includedNodes.isEmpty() ? "" :
            String.format("luc:include luc:setParam \"%s\".",
                includedNodes.stream().collect(Collectors.joining(" "))))
        .put("include-entities", includedEntities == null || includedEntities.isEmpty() ? "" :
            String.format(" luc:includeEntities luc:setParam \"%s\".",
                includedEntities.stream().collect(Collectors.joining(" "))))
        .put("include-predicates",
            includedPredicates == null || includedPredicates.isEmpty() ? "" : String
                .format("luc:includePredicates luc:setParam \"%s\".",
                    includedPredicates.stream().collect(Collectors.joining(" "))))
        .put("languages", languages == null || languages.isEmpty() ? ""
            : String.format("luc:languages luc:setParam \"%s\".",
                languages.stream().collect(Collectors.joining(","))))
        .put("molecule-size", moleculeSize == null ? ""
            : String.format("luc:moleculeSize luc:setParam \"%d\".", moleculeSize))
        .put("use-rdfrank",
            useRDFRank == null ? ""
                : String.format("luc:useRDFRank luc:setParam \"%s\".", useRDFRank))
        .put("analyser",
            analyser == null ? "" : String.format("luc:analyzer luc:setParam \"%s\".", analyser))
        .put("scorer",
            analyser == null ? "" : String.format("luc:scorer luc:setParam \"%s\".", scorer))
        .build()).replace(TRIPLE_CONFIG_TEMPLATE);
  }

  /**
   * Gets the name of the lucene index, which must form a valid IRI with the {@link
   * GraphDbLuceneConfig#GRAPHDB_LUCENE_NS} as prefix.
   *
   * @return the name of the lucene index.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the IRI of the lucene index.
   *
   * @return the IRI of the lucene index.
   */
  public String getLuceneIndexIRI() {
    return GRAPHDB_LUCENE_NS + name;
  }

  /**
   * Returns {@code true}, if the lucene index should be newly initialized, otherwise {@code
   * false}.
   *
   * @return {@code true}, if the lucene index should be newly initialized, otherwise {@code false}.
   */
  public boolean shouldBeInitialized() {
    return initialize;
  }

  /**
   * Gets a list of type of nodes. Indicates what kinds of nodes are to be indexed. The value can be
   * a list of values from: URI, literal, bnode (the plural forms are also allowed: URIs, literals,
   * bnodes).
   *
   * @return a list of type of nodes that shall be indexed.
   */
  public List<String> getIndexing() {
    return indexing;
  }

  /**
   * Provides a regular expression to identify nodes, which will be excluded from the molecule. Note
   * that for literals and URI local names the regular expression is case-sensitive.
   * <p/>
   * This example {@code "hello.*"} will cause matching URIs (e.g., {@code
   * <http://example.com/uri#helloWorld>}) and literals (e.g., {@code "hello world!")} not to be
   * included.
   *
   * @return a regular expression to identify nodes, which will be excluded from the molecule.
   */
  public String getExcludePattern() {
    return excludePattern;
  }

  /**
   * A list of entities that will NOT be included in an RDF molecule.
   * <p/>
   * This example {@code ["http://www.w3.org/2000/01/rdf-schema#Class",
   * "http://www.example.com/dummy#E1"]} includes any URI in a molecule, except the two listed.
   *
   * @return a list of entities that will NOT be included in an RDF molecule.
   */
  public List<String> getExcludedEntities() {
    return excludedEntities;
  }

  /**
   * A list of properties that will NOT be traversed in order to build an RDF molecule.
   * <p/>
   * This example {@code ["http://www.w3.org/2000/01/rdf-schema#subClassOf",
   * "http://www.example.com/dummy#p1"} prevents any entities being added to an RDF molecule, if
   * they can only be reached via the two given properties.
   *
   * @return a list of properties that will NOT be traversed in order to build an RDF molecule.
   */
  public List<String> getExcludedPredicates() {
    return excludedPredicates;
  }

  /**
   * Indicates what kinds of nodes are to be included in the molecule. The value can be a list of
   * values from: URI, literal, centre (the plural forms are also allowed: URIs, literals, centres).
   * The value of centre causes the node for which the molecule is built to be added to the molecule
   * (provided it is not a blank node). This can be useful, for example, when indexing URI nodes
   * with molecules that contain only literals, but the local part of the URI should also be
   * searchable.
   * <p/>
   * An example is {@code ["literal","uri"]}.
   *
   * @return a list of kinds of nodes that are included in the molecule.
   */
  public List<String> getIncludedNodes() {
    return includedNodes;
  }

  /**
   * A list of entities that can be included in an RDF molecule. Any other entities are ignored.
   * <p/>
   * This example {@code ["http://www.w3.org/2000/01/rdf-schema#Class",
   * "http://www.example.com/dummy#E1"]}builds molecules that only contain the two entities.
   */
  public List<String> getIncludedEntities() {
    return includedEntities;
  }

  /**
   * A list of properties that can be traversed in order to build an RDF molecule. The example below
   * allows any entities to be added to an RDF molecule, but only if they can be reached via the two
   * given properties.
   * <p/>
   * An example is {@code ["http://www.w3.org/2000/01/rdf-schema#subClassOf",
   * "http://www.example.com/dummy#p1"}.
   *
   * @return a list of properties that can be traversed in order to build an RDF molecule.
   */
  public List<String> getIncludedPredicates() {
    return includedPredicates;
  }

  /**
   * A list of language tags. Only literals with the indicated language tags are included in the
   * index. To include literals that have no language tag, a special value {@code none} is used.
   *
   * @return a list of language tags.
   */
  public List<String> getLanguages() {
    return languages;
  }

  /**
   * Gets the size of the molecule associated with each entity. A value of zero indicates that only
   * the entity itself should be indexed. A value of 1 indicates that the molecule will contain all
   * entities reachable by a single ‘hop’ via any predicate (predicates not included in the
   * molecule). Note that blank nodes are never included in the molecule. If a blank node is
   * encountered, the search is extended via any predicate to the next nearest entity and so on.
   * Therefore, even when the molecule size is 1, entities reachable via several intermediate
   * predicates can still be included in the molecule, if all the intermediate entities are blank
   * nodes. Molecule sizes of 2 and more are allowed, but with large datasets it can take a very
   * long time to create the index.
   *
   * @return the size of the molecule associated with each entity.
   */
  public Integer getMoleculeSize() {
    return moleculeSize;
  }

  /**
   * Indicates whether the RDF weights (if they have been already computed) associated with each
   * entity should be used as boosting factors when computing the relevance of a given Lucene query.
   * Allowable values are no, yes and squared. The last value indicates that the square of the RDF
   * Rank value is to be used.
   *
   * @return {@code "yes"}, {@code "no"} and {@code "none"}.
   */
  public String getUseRDFRank() {
    return useRDFRank;
  }

  /**
   * Gets an alternative analyser for processing text to produce terms to index. By default, this
   * parameter has no value and the default analyser used is: {@code org.apache.lucene.analysis.standard.StandardAnalyzer}
   * An alternative analyser must be derived from: {@code org.apache.lucene.analysis.Analyzer}. To
   * use an alternative analyser, use this parameter to identify the name of a Java factory class
   * that can instantiate it. The factory class must be available on the Java virtual machine’s
   * classpath and must implement this interface: {@code com.ontotext.trree.plugin.lucene.AnalyzerFactory}.
   * <p/>
   * The analyser must be on the classpath of the GraphDB instance, not of this middleware.
   *
   * @return an alternative analyser for processing text to produce terms to index.
   */
  public String getAnalyser() {
    return analyser;
  }

  /**
   * Gets an alternative scorer that provides boosting values, which adjust the relevance (and hence
   * the ordering) of results to a Lucene query. By default, this parameter has no value and no
   * additional scoring takes place, however, if the useRDFRank parameter is set to true, then the
   * RDF Rank scores are used. An alternative scorer must implement this interface: {@code
   * com.ontotext.trree.plugin.lucene.Scorer}. In order to use an alternative scorer, use this
   * parameter to identify the name of a Java factory class that can instantiate it. The factory
   * class must be available on the Java virtual machine’s classpath and must implement this
   * interface: {@code com.ontotext.trree.plugin.lucene.ScorerFactory}.
   * <p/>
   * The analyser must be on the classpath of the GraphDB instance, not of this middleware.
   */
  public String getScorer() {
    return scorer;
  }

  @Override
  public String toString() {
    return "GraphDbLuceneConfig{" +
        "name='" + name + '\'' +
        ", initialize=" + initialize +
        ", indexing=" + indexing +
        ", excludePattern='" + excludePattern + '\'' +
        ", excludedEntities=" + excludedEntities +
        ", excludedPredicates=" + excludedPredicates +
        ", includedNodes=" + includedNodes +
        ", includedEntities=" + includedEntities +
        ", includedPredicates=" + includedPredicates +
        ", moleculeSize=" + moleculeSize +
        ", languages=" + languages +
        ", useRDFRank='" + useRDFRank + '\'' +
        ", analyser='" + analyser + '\'' +
        ", scorer='" + scorer + '\'' +
        '}';
  }
}
