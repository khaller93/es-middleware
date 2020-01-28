package at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.exception.KGSPARQLResultFormatException;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.exception.KGSPARQLResultSerializationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class represents query results from a executed SPARQL query.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface QueryResult {

  /**
   * Iterates over the given list of {@code mimeTypes} and returns the first matching (supported)
   * mime type. If there is no supported type in the list, then {@code null will be returned}.
   *
   * @param mimeTypes for which the match shall be found.
   * @return the mime type, or {@code {@link Optional#empty()}}, if no mime type in the given list is
   * supported.
   */
  Optional<String> matchMimeType(List<String> mimeTypes);


  /**
   * Supplies a readable error message for the client to show which mime types are supported.
   *
   * @param mimeTypes list of unsupported mime types that could not be matched ({@link
   * QueryResult#matchMimeType(List)}).
   */
  Supplier<KGSPARQLResultFormatException> getMimeTypeException(List<String> mimeTypes);

  /**
   * Transforms the query result into the given {@code mimeType}. This method will be called
   * frequently, thus it should be implemented with efficiency in mind.
   *
   * @param mimeType MIME type of the format into which this result should be serialized.
   * @return the serialized query result in the given mime type.
   * @throws KGSPARQLResultFormatException if the given mime type is not supported by this query
   * result.
   * @throws KGSPARQLResultSerializationException if the result could not be serialized.
   */
  byte[] transform(String mimeType)
      throws KGSPARQLResultFormatException, KGSPARQLResultSerializationException;

  /**
   * Transforms the query result into the first format that is supported in the given list of {@code
   * mimeTypes}. This method will be called frequently, thus it should be implemented with
   * efficiency in mind.
   *
   * @param mimeTypes a list of MIME types into which this result should potentially be serialized.
   * @return the serialized query result in the first supported mime type.
   * @throws KGSPARQLResultFormatException if the given mime types are not supported by this query
   * result.
   * @throws KGSPARQLResultSerializationException if the result could not be serialized.
   */
  byte[] transform(List<String> mimeTypes)
      throws KGSPARQLResultFormatException, KGSPARQLResultSerializationException;

}
