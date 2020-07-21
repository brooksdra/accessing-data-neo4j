package com.protedyne.accessingdataneo4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
//import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Dependency {

  @Id @GeneratedValue private Long id;

  private String definition;
  //@Property(name="name")
  private String artifactId;
  private String groupId;
  private String packageType;
  private String version;
  private String scope;

  @SuppressWarnings("unused")
  private Dependency() {
    // Empty constructor required as of Neo4j API 2.0.5
  };

  public Dependency(String definition) {
    this.definition = definition;
    String[] parsed = definition.split(":");
    this.groupId = parsed[0];
    this.artifactId = parsed[1];
    this.packageType = parsed[2];
    this.version = parsed[3];
    if (parsed.length > 4) {
      scope = parsed[4];
    }
  }

  public Dependency(String groupId, String artifactId, String packageType, String version, String scope) {
    this.definition = groupId + ":" + artifactId + ":" + packageType + ":" + version;
    if (scope != null) {
      this.definition = this.definition + ":" + scope;
    }
    this.artifactId = artifactId;
    this.groupId = groupId;
    this.packageType = packageType;
    this.version = version;
    this.scope = scope;
  }

  /**
   * Neo4j doesn't REALLY have bi-directional relationships. It just means when querying
   * to ignore the direction of the relationship.
   * https://dzone.com/articles/modelling-data-neo4j
   */
  @Relationship(type = "DEPENDS_ON", direction = Relationship.OUTGOING)
  public Set<Dependency> dependencies;

  public void dependsOn(Dependency dependency) {
    if (this.dependencies == null) {
      this.dependencies = new HashSet<>();
    }
    this.dependencies.add(dependency);
  }

  public String toString() {

    return this.artifactId + "'s depends_on => "
      + Optional.ofNullable(this.dependencies).orElse(
          Collections.emptySet()).stream()
            .map(Dependency::getArtifactId)
            .collect(Collectors.toList());
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getPackageType() {
    return packageType;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

}