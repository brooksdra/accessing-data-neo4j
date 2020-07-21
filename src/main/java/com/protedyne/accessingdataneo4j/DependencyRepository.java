package com.protedyne.accessingdataneo4j;

import org.springframework.data.neo4j.annotation.Query;
//import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.List;

public interface DependencyRepository extends Neo4jRepository<Dependency, Long> {

  Dependency findByDefinition(String definition);

  List<Dependency> findByGroupId(String groupId);
  
  Dependency findByArtifactId(String artifactId);


  // example MATCH (movie:Movie {title={0}})<-[:ACTS_IN]-(actor) RETURN actor
  @Query("MATCH (target:Dependency {artifactId: $artifactId})<-[*]-(deps) RETURN deps")
  List<Dependency> getDeepDependenciesFor(String artifactId);

    // @QueryResult
    // public class MovieData {
    //     Movie movie;
    //     Double averageRating;
    //     Set<Actor> cast;
    // }
}