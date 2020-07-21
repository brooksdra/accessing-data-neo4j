package com.protedyne.accessingdataneo4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

//@SpringBootApplication
//@EnableNeo4jRepositories
public class ParseDepTreeIntoNeo4j {

	private final static Logger log = LoggerFactory.getLogger(ParseDepTreeIntoNeo4j.class);

	public final static String DEP_ON = "+-";
	public final static String DEP_SUB = "| ";
	public final static String DEP_END = "\\-";
	public final static String DEP_END_W_DEP = "  ";

	public final static int LINE_IND_LEN = 2;
	public final static int LINE_IND_OFFSET = 3; 

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ParseDepTreeIntoNeo4j.class, args);
	}

	@Bean
	CommandLineRunner parse(DependencyRepository dependencyRepository) {
		return args -> {

			dependencyRepository.deleteAll();
			
			String filename = "test.txt";

			Stack<Dependency> stack = new Stack<>();
			Dependency currentDependent = null;
			Dependency currentParent = null;
			int offset = 0;

			// use list instead of stream as we need method scoped variables
			List<String> lines = Files.readAllLines(Paths.get(filename));
			for (String line : lines) {			
				log.info(line);                    
				if (stack.isEmpty()) {
					// This is the root project
					currentDependent = new Dependency(line.trim());
					log.info("  Added to stack as root: " + currentDependent.getDefinition());  
					dependencyRepository.save(currentDependent);
					stack.push(currentDependent);
				}
				else {
					//calculate line indicator offset
					String marker = line.substring(offset, offset + LINE_IND_LEN);
					if (marker.equals(DEP_ON)) {
						// This is a current dependent of current parent
						// Just added it to the current parent
						currentParent = stack.lastElement();
						log.info("  current dependency of " + currentParent.getDefinition());							
						currentDependent = new Dependency(line.substring(offset + LINE_IND_OFFSET));
						dependencyRepository.save(currentDependent);
						currentParent.dependsOn(currentDependent);
						dependencyRepository.save(currentParent);
					}
					else if (marker.equals(DEP_END)) {
						// This is the last dependent of current parent
						// Added it to the current parent
						currentParent = stack.lastElement();
						log.info("  last dependency of " + currentParent.getDefinition());							
						currentDependent = new Dependency(line.substring(offset + LINE_IND_OFFSET));
						dependencyRepository.save(currentDependent);
						currentParent.dependsOn(currentDependent);
						dependencyRepository.save(currentParent);
						
						// Go up one level of hierarchy
						stack.pop();
						offset -= LINE_IND_OFFSET;
					}
					else if (marker.equals(DEP_SUB)) {
						// This is the first dependent of current new parent
						// Make previous current dependent the new parent
						stack.push(currentDependent);
						offset += LINE_IND_OFFSET;
						// Update to the current marker
						marker = line.substring(offset, offset + LINE_IND_LEN);
						if (marker.equals(DEP_ON)) {
							// This is a current dependent of current parent
							// Just added it to the current parent
							currentParent = stack.lastElement();
							log.info("  current dependency of " + currentParent.getDefinition());							
							currentDependent = new Dependency(line.substring(offset + LINE_IND_OFFSET));
							dependencyRepository.save(currentDependent);
							currentParent.dependsOn(currentDependent);
							dependencyRepository.save(currentParent);
						}
						else if (marker.equals(DEP_END)) {
							// This is only dependent of current parent
							// Added it to the current parent
							currentParent = stack.lastElement();
							log.info("  last dependency of " + currentParent.getDefinition());							
							currentDependent = new Dependency(line.substring(offset + LINE_IND_OFFSET));
							dependencyRepository.save(currentDependent);
							currentParent.dependsOn(currentDependent);
							dependencyRepository.save(currentParent);
							// Go up one level of hierarchy
							stack.pop();
							offset -= LINE_IND_OFFSET;
						}
					}
				}
			}

			// try (Stream<String> stream = Files.lines(Paths.get(filename))) {
			// 	stream.forEach( line ->  {
			// 		log.info(line);                    
			// 		if (stack.isEmpty()) {
			// 			log.info("  Added to stack as root.");  
			// 			Dependency dependency = new Dependency(line);
			// 			dependencyRepository.save(dependency);
			// 			stack.push(dependency);
			// 		}
			// 		else {
			// 			//calculate line indicator offset
			// 			int offset = (stack.size() - 1) * OFFSET;
			// 			String marker = line.substring(offset, offset + 2);
			// 			if (marker.equals(DEP_ON)) {
			// 				// make way for the next dependent unless at root
			// 				if (stack.size() > 1) {
			// 					stack.pop();
			// 				}

			// 				//gets added to the current dependent only
			// 				log.info("  current dependency");							
			// 				Dependency dependency = new Dependency(line.substring(offset + OFFSET));
			// 				dependencyRepository.save(dependency);
			// 				stack.firstElement().dependsOn(dependency);
			// 				dependencyRepository.save(stack.firstElement());
			// 				// push on stack for next time							
			// 				stack.push(dependency);
			// 			}
			// 			else if (marker.equals(DEP_END)){
			// 				// make way for the next dependent
			// 				stack.pop();

			// 				log.info("  last dependency");					
			// 				Dependency dependency = new Dependency(line.substring(offset + OFFSET));
			// 				dependencyRepository.save(dependency);
			// 				stack.firstElement().dependsOn(dependency);
			// 				dependencyRepository.save(stack.firstElement());
			// 				// no need to pop this is the end
			// 				//stack.pop();
			// 			}
			// 			else if (marker.equals(DEP_SUB)) {
			// 				// no need to pop this is now the current level
			// 				//stack.pop();
			// 				int newOffset = offset + OFFSET;
							
			// 				if (marker.equals(DEP_ON)) {
			// 					//gets added to the current dependent only
			// 					log.info("  current dependency");							
			// 					Dependency dependency = new Dependency(line.substring(newOffset + OFFSET));
			// 					dependencyRepository.save(dependency);
			// 					stack.firstElement().dependsOn(dependency);
			// 					dependencyRepository.save(stack.firstElement());
			// 					// push on stack for next time							
			// 					stack.push(dependency);
			// 				}
			// 				else if (marker.equals(DEP_END)) {
			// 					//add to the current dependent 
			// 					log.info("  last dependency");					
			// 					Dependency dependency = new Dependency(line.substring(offset + OFFSET));
			// 					dependencyRepository.save(dependency);
			// 					stack.firstElement().dependsOn(dependency);
			// 					dependencyRepository.save(stack.firstElement());
			// 					// no need to pop this is the end
			// 					//stack.pop();
			// 				}
			// 			}
			// 		}
			// 	});

			// 	// stream.filter(line -> !line.contains("{") && !line.contains("}") )
			// 	// 	.forEach( line ->  {
			// 	// 		String[] parts = line.split("\s->\s");
			// 	// 		String parent = parts[0].trim();
			// 	// 		//System.out.println(parent);
			// 	// 		String depent = parts[1].trim();
			// 	// 		//System.out.println(depent);
			// 	// 		List<String> list = ref.get(parent);
			// 	// 		if (list == null) {
			// 	// 			list = new ArrayList<>();
			// 	// 			ref.put(parent, list);
			// 	// 		}
			// 	// 		list.add(depent);                    
			// 	// 	});
	
			// } catch (IOException e) {
			// 	log.error ("problem", e);
			// 	e.printStackTrace();
			// }

			// Person greg = new Person("Greg");
			// Person roy = new Person("Roy");
			// Person craig = new Person("Craig");

			// List<Person> team = Arrays.asList(greg, roy, craig);

			// log.info("Before linking up with Neo4j...");

			// team.stream().forEach(person -> log.info("\t" + person.toString()));

			// personRepository.save(greg);
			// personRepository.save(roy);
			// personRepository.save(craig);

			// greg = personRepository.findByName(greg.getName());
			// greg.worksWith(roy);
			// greg.worksWith(craig);
			// personRepository.save(greg);

			// roy = personRepository.findByName(roy.getName());
			// roy.worksWith(craig);
			// // We already know that roy works with greg
			// personRepository.save(roy);

			// // We already know craig works with roy and greg

			// log.info("Lookup each person by name...");
			// team.stream().forEach(person -> log.info(
			// 		"\t" + personRepository.findByName(person.getName()).toString()));
		};
	}

}