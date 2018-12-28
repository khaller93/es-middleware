package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.schema;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

/**
 * This class represents a description of an used property for the property graph schema. It can
 * either be predefined token or a property key.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class PGSProp {

  private Object identifier;
  private boolean isToken;
  private boolean isPropertyKey;

  /**
   * Creates a new {@link PGSProp} for the given object.
   *
   * @param identifier for which the schema property shall be created.
   */
  private PGSProp(Object identifier) {
    this.identifier = identifier;
    this.isToken = identifier instanceof T;
    this.isPropertyKey = identifier instanceof String;
    if (!isToken && !isPropertyKey) {
      throw new IllegalArgumentException(
          String.format("The given property object must be a String or '%s'.",
              T.class));
    }
  }

  /**
   * Creates a new {@link PGSProp} for the given object, which represents the identifier of the
   * property (String or Token).
   *
   * @param identifier for which the schema property shall be created.
   * @return the created {@link PGSProp}.
   * @throws IllegalArgumentException if the given {@code prop} is not a {@link String} or a {@link
   * T}.
   */
  public static PGSProp of(Object identifier) {
    return new PGSProp(identifier);
  }

  /**
   * Gets the property identifier, which can be a property key (string) or a token.
   *
   * @return the the property identifier, which can be a property key (string) or a token.
   */
  public Object identifier() {
    return identifier;
  }

  /**
   * Gets the string form of the property identifier.
   *
   * @return the the property identifier, which can be a property key (string) or a token.
   */
  public String identifierAsString() {
    return isToken ? ((T) identifier).getAccessor() : (String) identifier;
  }

  /**
   * Checks whether the property is a token {@link T#id}, {@link T#label}, {@link T#key}, or {@link
   * T#value}.
   *
   * @return {@code true}, if the property is an accessor {@link T#id}, {@link T#label}, {@link
   * T#key}, or {@link T#value}. Otherwise {@code false}.
   */
  public boolean isToken() {
    return isToken;
  }

  /**
   * Checks whether this a property key.
   *
   * @return {@code true}, if this is a property key, otherwise {@code false}.
   */
  public boolean isPropertyKey() {
    return isPropertyKey;
  }

  /**
   * Looks at the given {@code element} and returns the value of this property.
   *
   * @param element for which the value of this property shall be returned.
   * @param <V> the type of the accessed value.
   * @return the value for this property of the given element.
   */
  public <V> V apply(Element element) {
    if (isToken) {
      return (V) ((T) identifier).apply(element);
    } else {
      return (V) element.value((String) identifier);
    }
  }

}
