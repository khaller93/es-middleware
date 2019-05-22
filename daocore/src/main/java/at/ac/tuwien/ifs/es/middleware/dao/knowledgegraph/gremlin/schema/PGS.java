package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema;

/**
 * This class represents the property graph schema that is used to cover RDF data.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class PGS {

  private PGSProp kind;
  private PGSProp iri;
  private PGSProp bnodeId;
  private LiteralGraphSchema literal;

  /**
   * Creates a new schema for a property graph representing RDF data.
   *
   * @param kind {@link PGSProp} for holding the kind of resource.
   * @param iri {@link PGSProp} for specifying IRIs.
   * @param bnodeId {@link PGSProp} for specifying the ID of blank nodes.
   * @param literal {@link LiteralGraphSchema} for literals.
   */
  private PGS(PGSProp kind, PGSProp iri, PGSProp bnodeId, LiteralGraphSchema literal) {
    this.kind = kind;
    this.iri = iri;
    this.bnodeId = bnodeId;
    this.literal = literal;
  }

  /**
   * Creates a new schema for a property graph representing RDF data.
   *
   * @param kind Token or String for the property holding the kind of resource.
   * @param iri Token or String for the property specifying IRIs.
   * @param bnodeId Token or String for the property specifying the ID of blank nodes.
   * @param literal {@link LiteralGraphSchema} for literals.
   */
  public static PGS with(Object kind, Object iri, Object bnodeId, LiteralGraphSchema literal) {
    return new PGS(PGSProp.of(kind), PGSProp.of(iri), PGSProp.of(bnodeId), literal);
  }

  /**
   * Gets the {@link PGSProp} for the kind of resource (IRI, Blanknode, RDFLiteral).
   *
   * @return the {@link PGSProp} for the kind of resource (IRI, Blanknode, RDFLiteral).
   */
  public PGSProp kind() {
    return kind;
  }

  /**
   * Gets the {@link PGSProp} for IRIs.
   *
   * @return the {@link PGSProp} for IRIs.
   */
  public PGSProp iri() {
    return iri;
  }

  /**
   * Gets the {@link PGSProp} for the ID of blank nodes.
   *
   * @return the {@link PGSProp} for the ID of blank nodes.
   */
  public PGSProp bnodeId() {
    return bnodeId;
  }

  /**
   * Gets the schema for RDF literals.
   *
   * @return the schema for literals ({@link LiteralGraphSchema}).
   */
  public LiteralGraphSchema literal() {
    return literal;
  }
}
