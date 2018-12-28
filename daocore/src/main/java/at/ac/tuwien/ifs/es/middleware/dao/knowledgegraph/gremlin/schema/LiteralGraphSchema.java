package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class LiteralGraphSchema {

  private Object valueProptertyName;
  private Object datatypePropertyName;
  private Object languagePropertyName;

  public LiteralGraphSchema(Object valueProptertyName, Object datatypePropertyName,
      Object languagePropertyName) {
    this.valueProptertyName = valueProptertyName;
    this.datatypePropertyName = datatypePropertyName;
    this.languagePropertyName = languagePropertyName;
  }

  public Object getValueProptertyName() {
    return valueProptertyName;
  }

  public Object getDatatypePropertyName() {
    return datatypePropertyName;
  }

  public Object getLanguagePropertyName() {
    return languagePropertyName;
  }
}
