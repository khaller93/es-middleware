package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

/**
 * <p/>
 * The object identity is used as id for {@link KnowledgeGraphObserver}s. Hence, a deep copy of or
 * proxy for an observer with a different object identity will not unsubscribe the original,
 * subscribed observer.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KnowledgeGraphObservable {

  /**
   * Subscribes the given {@link KnowledgeGraphObserver} to be notified if the corresponding
   * knowledge graph changes.
   *
   * @param knowledgeGraphObserver {@link KnowledgeGraphObserver} that shall be subscribed.
   */
  void subscribe(KnowledgeGraphObserver knowledgeGraphObserver);

  /**
   * Unscribes the given {@link KnowledgeGraphObserver}.
   *
   * @param knowledgeGraphObserver {@link KnowledgeGraphObserver} that shall be unsubscribed.
   */
  void unsubscribe(KnowledgeGraphObserver knowledgeGraphObserver);

}
