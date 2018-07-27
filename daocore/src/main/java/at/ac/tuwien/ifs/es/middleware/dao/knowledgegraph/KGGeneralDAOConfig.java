package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import org.springframework.context.ApplicationContext;

/**
 * This class is intended to provide the repetitive code that would be needed in most {@link
 * KnowledgeGraphDAOConfig}s. The special implementation of this class has to provide the default
 * beans, when no choice is given.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class KGGeneralDAOConfig<S extends KGSparqlDAO, F extends KGFullTextSearchDAO, G extends KGGremlinDAO> implements
    KnowledgeGraphDAOConfig {

  private ApplicationContext context;

  private String sparqlChoice;
  private String ftsChoice;
  private String gremlinChoice;

  /**
   * Creates a new {@link KGGeneralDAOConfig}, which uses the given {@code context} and choices.
   *
   * @param context which shall be used to get the beans.
   * @param sparqlChoice the name of the bean, which shall be used as {@link KGSparqlDAO}.
   * @param ftsChoice the name of the bean, which shall be used as {@link KGFullTextSearchDAO}.
   * @param gremlinChoice the name of the bean, which shall be used as {@link KGGremlinDAO}.
   */
  public KGGeneralDAOConfig(ApplicationContext context, String sparqlChoice, String ftsChoice,
      String gremlinChoice) {
    this.context = context;
    this.sparqlChoice = sparqlChoice;
    this.ftsChoice = ftsChoice;
    this.gremlinChoice = gremlinChoice;
  }

  /**
   * Gets the default {@link KGSparqlDAO}, which is used, when no specific choice is given.
   *
   * @return the default {@link KGSparqlDAO}.
   */
  protected abstract S getDefaultSparqlDAO();

  /**
   * Gets the (super)class representing the minimum requirements.
   *
   * @return the (super)class representing the minimum requirements.
   */
  protected abstract Class<S> getSparqlDAOClass();

  @Override
  public S getSparqlDAO() {
    if (sparqlChoice == null || sparqlChoice.isEmpty()) {
      return getDefaultSparqlDAO();
    } else {
      return context.getBean(sparqlChoice, getSparqlDAOClass());
    }
  }

  /**
   * Gets the default {@link KGFullTextSearchDAO}, which is used, when no specific choice is given.
   *
   * @return the default {@link KGFullTextSearchDAO}.
   */
  protected abstract F getDefaultFullTextSearchDAO();

  /**
   * Gets the (super)class representing the minimum requirements.
   *
   * @return the (super)class representing the minimum requirements.
   */
  protected abstract Class<F> getFullTextSearchDAOClass();

  @Override
  public F getFullTextSearchDAO() {
    if (ftsChoice == null || ftsChoice.isEmpty()) {
      return getDefaultFullTextSearchDAO();
    } else {
      return context.getBean(ftsChoice, getFullTextSearchDAOClass());
    }
  }

  /**
   * Gets the default {@link KGGremlinDAO}, which is used, when no specific choice is given.
   *
   * @return the default {@link KGGremlinDAO}.
   */
  protected abstract G getDefaultGremlinDAO();

  /**
   * Gets the (super)class representing the minimum requirements.
   *
   * @return the (super)class representing the minimum requirements.
   */
  protected abstract Class<G> getGremlinDAOClass();

  @Override
  public G getGremlinDAO() {
    if (gremlinChoice == null || gremlinChoice.isEmpty()) {
      return getDefaultGremlinDAO();
    } else {
      return context.getBean(gremlinChoice, getGremlinDAOClass());
    }
  }
}
