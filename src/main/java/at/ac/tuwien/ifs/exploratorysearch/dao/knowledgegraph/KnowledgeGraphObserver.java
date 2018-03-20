package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

/**
 * This observer listen to changes made to a certain knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface KnowledgeGraphObserver {

  /**
   * This method is called, if the knowledge graph has changed; e.g. triples have been added,
   * updated or removed.
   */
  void onChange();

}
