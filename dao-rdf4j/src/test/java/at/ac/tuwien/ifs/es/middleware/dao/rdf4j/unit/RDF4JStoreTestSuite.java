package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit;

import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.memory.RDF4JMemoryStoreWithLuceneMusicPintaGremlinTests;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.memory.RDF4JMemoryStoreWithLuceneMusicPintaSPARQLTests;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.nativerdf.RDF4JNativeStoreWithLuceneMusicPintaGremlinTests;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit.store.nativerdf.RDF4JNativeStoreWithLuceneMusicPintaSPARQLTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    RDF4JMemoryStoreWithLuceneMusicPintaSPARQLTests.class,
    RDF4JMemoryStoreWithLuceneMusicPintaGremlinTests.class,
    RDF4JNativeStoreWithLuceneMusicPintaGremlinTests.class,
    RDF4JNativeStoreWithLuceneMusicPintaSPARQLTests.class
})
public class RDF4JStoreTestSuite {

}
