package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.AllClassesServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.ClassHierarchyTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.DegreeCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.PageRankCentralityMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.PeerPressureClusteringMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classentropy.ClassEntropyWithGremlinServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classentropy.ClassEntropyWithSPARQLServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classresource.ClassResourceWithGremlinServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.lca.LCAOnTheFlyServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.lca.LCSWithClassHierarchyServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.lcapr.LCAPRMetricImplTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.lcapr.LCAPRMetricOnTheFlyServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.resnik.ResnikSimilarityMetricImplServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.SameAsResourceServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.UnweightedLDSDMetricServiceTests;
import at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.resnik.ResnikSimilarityOnTheFlyServiceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite is testing analysis services on the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(Suite.class)
@SuiteClasses({LCAOnTheFlyServiceTests.class, LCSWithClassHierarchyServiceTests.class,
    ResnikSimilarityOnTheFlyServiceTests.class, ClassHierarchyTests.class,
    LCAPRMetricOnTheFlyServiceTests.class, LCAPRMetricImplTests.class,
    ResnikSimilarityMetricImplServiceTests.class, AllClassesServiceTests.class,
    ClassEntropyWithSPARQLServiceTests.class, ClassEntropyWithGremlinServiceTests.class,
    DegreeCentralityMetricServiceTests.class, PageRankCentralityMetricServiceTests.class,
    PeerPressureClusteringMetricServiceTests.class, SameAsResourceServiceTests.class,
    UnweightedLDSDMetricServiceTests.class, ClassResourceWithGremlinServiceTests.class})
public class AnalysisServiceTestSuite {

}
