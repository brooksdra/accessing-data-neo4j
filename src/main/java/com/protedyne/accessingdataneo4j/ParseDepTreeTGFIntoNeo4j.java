package com.protedyne.accessingdataneo4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories
public class ParseDepTreeTGFIntoNeo4j {

	private final static Logger log = LoggerFactory.getLogger(ParseDepTreeTGFIntoNeo4j.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ParseDepTreeTGFIntoNeo4j.class, args);
	}

	@Bean
	CommandLineRunner parse(DependencyRepository dependencyRepository) {
		return args -> {

			boolean reloadData = false;
			if (reloadData) {
				dependencyRepository.deleteAll();
				
				String[] filenames = {"MatrixActivityManager.tgf", "BinTrackerService.tgf" };
				for (String filename : filenames) {
					Map<String, Dependency> dependencies = new HashMap<>();
	
					boolean relating = false;
		
					// Process the list of dependencies
					List<String> lines = Files.readAllLines(Paths.get(filename));
					for (String line : lines) {			
						log.info(line);   
						
						if (line.startsWith("#")) {
							relating = true;
							continue;
						}
		
						if (relating) {
							// relating dependency nodes
							String[] relDef = line.split(" ");
							String parentRef = relDef[0];
							String childRef = relDef[1];
							//String scope = relDef[2];
							Dependency parent = dependencies.get(parentRef);
							if (parent != null) {
								Dependency child = dependencies.get(childRef);
								if (child != null) {
									parent.dependsOn(child);
									// Do we need to resave parent node? Yes
									dependencyRepository.save(parent);
									// Can we do something with scope?
								}
							}
						}
						else {
							// Building dependent nodes and save to neo4j
							String[] nodeDef = line.split(" ");
							String ref = nodeDef[0];
							String definition = nodeDef[1];
							Dependency dependency = dependencyRepository.findByDefinition(definition);
							if (dependency == null) {
								dependency = new Dependency(definition);
								dependencyRepository.save(dependency);
								log.info("Add node for " + dependency.getArtifactId());  
							}
							// Save for reference phase
							dependencies.put(ref, dependency);
						}
					}
				}
			}

			log.info(" ");
			log.info("Let's do some reporting...");
			log.info("Dependencies from groupId com.protedyne");
			List<Dependency> deps = dependencyRepository.findByGroupId("com.protedyne");
			for (Dependency dep : deps) {
				log.info("" + dep);
			}

			log.info(" ");
			log.info("Let's do some more specific reporting...");
			log.info("Find the dependencies for BinTracker...");
			Dependency binTracker = dependencyRepository.findByArtifactId("BinTrackerService");
			if (binTracker != null) {
				for (Dependency debinTrackerDep : binTracker.dependencies) {
					log.info("" + debinTrackerDep);
				}
			}
			
			log.info(" ");
			log.info("Find all users of log4j...");
			List<Dependency>  deepDeps = dependencyRepository.getDeepDependenciesFor("log4j");
			for (Dependency deepDep : deepDeps) {				
				log.info("" + deepDep);				
			}
			
			
		};
	}
}