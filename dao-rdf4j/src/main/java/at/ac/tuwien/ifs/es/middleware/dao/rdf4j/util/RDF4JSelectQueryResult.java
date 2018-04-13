package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.util;

import at.ac.tuwien.ifs.es.middleware.dto.exception.SPARQLResultFormatException;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes the {@link TupleQueryResult} of a SPARQL query executed with the RDF4J framework
 * and abstracts it to {@link SelectQueryResult}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class RDF4JSelectQueryResult extends RDF4JQueryResult<QueryResultFormat> implements
    SelectQueryResult {

  private static final Logger logger = LoggerFactory.getLogger(
      RDF4JSelectQueryResult.class);

  private static final List<QueryResultFormat> SELECT_QUERY_RESULT_FORMATS = Arrays
      .asList(TupleQueryResultFormat.SPARQL, TupleQueryResultFormat.JSON,
          TupleQueryResultFormat.CSV, TupleQueryResultFormat.TSV, TupleQueryResultFormat.BINARY);

  private static final String SELECT_QUERY_RESULT_FORMATS_STRING = RDF4JQueryResult
      .transformResultFormatsToReadableString(SELECT_QUERY_RESULT_FORMATS);

  private List<String> bindingNames;
  private List<BindingSet> bindingSets;

  public RDF4JSelectQueryResult(List<String> bindingNames, List<BindingSet> bindingSets) {
    super(SELECT_QUERY_RESULT_FORMATS, SELECT_QUERY_RESULT_FORMATS_STRING);
    this.bindingNames = bindingNames;
    this.bindingSets = bindingSets;
  }

  @Override
  public Table<Integer, String, RDFTerm> value() {
    Table<Integer, String, RDFTerm> resultTable = HashBasedTable.create();
    RDF4J valueFactory = new RDF4J();
    for (int i = 0; i < bindingSets.size(); i++) {
      for (String bindingName : bindingNames) {
        resultTable.put(i, bindingName,
            valueFactory.asRDFTerm(bindingSets.get(i).getBinding(bindingName).getValue()));
      }
    }
    return resultTable;
  }

  @Override
  public byte[] performTransformation(QueryResultFormat format) throws SPARQLResultFormatException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      QueryResults
          .report(new IteratingTupleQueryResult(bindingNames, bindingSets),
              QueryResultIO.createTupleWriter(format, out));
      return out.toByteArray();
    } catch (IOException e) {
      throw new SPARQLResultFormatException(e);
    }
  }

  @Override
  public String toString() {
    return "RDF4JSelectQueryResult{" +
        "bindingNames=" + bindingNames +
        ", bindingSets=" + bindingSets +
        '}';
  }
}
