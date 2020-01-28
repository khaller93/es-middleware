package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.AskQueryResult;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.sparql.exception.KGSPARQLResultFormatException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;

/**
 * This class is an implementation of {@link AskQueryResult} that maintains the response of a ASK
 * SPARQL query. This is either {@code true}, or {@code false}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RDF4JAskQueryResult extends RDF4JQueryResult<QueryResultFormat> implements
    AskQueryResult {

  private static final List<QueryResultFormat> ASK_QUERY_RESULT_FORMATS = Arrays
      .asList(BooleanQueryResultFormat.SPARQL, BooleanQueryResultFormat.JSON,
          BooleanQueryResultFormat.TEXT);

  private static final String ASK_QUERY_RESULT_FORMATS_STRING = RDF4JQueryResult
      .transformResultFormatsToReadableString(ASK_QUERY_RESULT_FORMATS);

  private boolean value;

  public RDF4JAskQueryResult(boolean value) {
    super(ASK_QUERY_RESULT_FORMATS, ASK_QUERY_RESULT_FORMATS_STRING);
    this.value = value;
  }

  /**
   * Returns the responded value of an ASK SPARQL query. This is either {@code true}, or {@code
   * false}.
   *
   * @return the responded value of an ASK SPARQL query.
   */
  public boolean value() {
    return value;
  }

  @Override
  public byte[] performTransformation(QueryResultFormat format) throws KGSPARQLResultFormatException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      QueryResultIO.createBooleanWriter(format, out).handleBoolean(value);
      return out.toByteArray();
    } catch (IOException e) {
      throw new KGSPARQLResultFormatException(e);
    }
  }

  @Override
  public String toString() {
    return "RDF4JAskQueryResult{" + value + "}";
  }
}
