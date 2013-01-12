/*©Copyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.agent;

import java.util.Iterator;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repastcity3.environment.Building;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;
import repastcity3.main.MODEL_PARAMETERS;

/**
 * Create agents. There are three methods that can be used to create agents: randomly create a number of agents, create
 * agents from a point shapefile or create a certain number of agents per neighbourhood specified in an area shapefile.
 * 
 * <P>
 * The method to use is specified by the 'agent_definition' parameter in <code>parameters.xml</code>. The parameter
 * takes the following form:
 * </P>
 * 
 * <pre>
 * {@code
 * <method>:<definition>
 * }
 * </pre>
 * 
 * <P>
 * where method and can be one of the following:
 * </P>
 * 
 * <ul>
 * <li>
 * 
 * <pre>
 * {@code random:<num_agents>}
 * </pre>
 * 
 * Create 'num_agents' agents in randomly chosen houses. The agents are of type <code>DefaultAgent</code>. For example,
 * this will create 10 agents in randomly chosen houses: '<code>random:1</code>'. See the
 * <code>createRandomAgents</code> function for implementation details.</li>
 * 
 * <li>
 * 
 * <pre>
 * {@code point:<filename>%<agent_class>}
 * </pre>
 * 
 * Create agents from the given point shapefile (one agent per point). If a point in the agent shapefile is within a
 * building object then the agent's home will be set to that building. The type of the agent can be given in two ways:
 * <ol>
 * <li>The 'agent_class' parameter can be used - this is the fully qualified (e.g. including package) name of a class
 * that will be used to create all the agents. For example the following will create instances of <code>MyAgent</code>
 * at each point in the shapefile '<code>point:data/my_shapefile.shp$my_package.agents.MyAgent</code>'.</li>
 * <li>A String column in the input shapefile called 'agent_type' provides the class of the agents. IIn this manner
 * agents of different types can be created from the same input. For example, the following will read the shapefile and
 * look at the values in the 'agent_type' column to create agents: '<code>point:data/my_shapefile.shp</code>' (note that
 * unlike the previous method there is no '$').</li>
 * </ol>
 * 
 * See the <code>createPointAgents</code> function for implementation details.
 * 
 * <li>
 * 
 * <pre>
 * {@code area:<filename>$BglrC1%<agent_class1>$ .. $BglrC5%<agent_class5>}
 * </pre>
 * 
 * Create agents from the given areas shapefile. Up to five different types of agents can be created. Columns in the
 * shapefile specify how many agents of each type to create per area and the agents created are randomly assigned to
 * houses withing their area. The columns names must follow the format 'BglrCX' where 1 <= X <= 5. For example the
 * following string:<br>
 * 
 * <pre>
 * {@code area:area.shp$BglrC1%BurglarAgent$BglrC2%EmployedAgent}
 * </pre>
 * 
 * will read the <code>area.shp</code> and, for each area, create a number of <code>BurglarAgent</code> and
 * <code>EmployedAgent</code> agents in each area, the number being specied by columns called <code>BglrC1</code> and
 * <code>BglrC2</code> respectively. See the <code>createAreaAgents</code> function for implementation details.</li>
 * </ul>
 * 
 * @author Nick Malleson
 * @see DefaultAgent
 */
public class AgentFactory {

	private static Logger LOGGER = Logger.getLogger(AgentFactory.class.getName());

	/** The method to use when creating agents (determined in constructor). */
	private AGENT_FACTORY_METHODS methodToUse;
	
	private int INIT_INFECTED = GlobalVars.INITIAL_INFECTED;

	/** The definition of the agents - specific to the method being used */
	//private String definition;

	/**
	 * Create a new agent factory from the given definition.
	 * 
	 * @param agentDefinition
	 */
	public AgentFactory(String agentDefinition) throws AgentCreationException {

		String method = agentDefinition;
		// First try to parse the definition
		//String[] split = agentDefinition.split(":");
		//if (split.length != 2) {
			//throw new AgentCreationException("Problem parsin the definition string '" + agentDefinition
					//+ "': it split into " + split.length + " parts but should split into 2.");
		//}
		//String method = split[0]; // The method to create agents
		//String defn = split[1]; // Information about the agents themselves

		if (method.equals(AGENT_FACTORY_METHODS.RANDOM.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.RANDOM;

		} /*else if (method.equals(AGENT_FACTORY_METHODS.POINT_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.POINT_FILE;
		}
*/
		else if (method.equals(AGENT_FACTORY_METHODS.AREA_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.AREA_FILE;
		}

		else {
			throw new AgentCreationException("Unrecognised method of creating agents: '" + method
					+ "'. Method must be " + AGENT_FACTORY_METHODS.RANDOM.toString() + " ! "
					);
		}

		//this.definition = defn; // Method is OK, save the definition for creating agents later.

		// Check the rest of the definition is also correct (passing false means don't create agents)
		// An exception will be thrown if it doesn't work.
		this.methodToUse.createAgMeth().createagents(false, this);
	}

	public void createAgents(Context<? extends IAgent> context) throws AgentCreationException {
		this.methodToUse.createAgMeth().createagents(true, this);
	}

	private void createSanemoriAgents(boolean dummy) throws AgentCreationException {
		// Check the definition is as expected, in this case it should be a number

		int numAgents = -1;
/*
		try {
			numAgents = Integer.parseInt(this.definition);
		} catch (NumberFormatException ex) {
			throw new AgentCreationException("Using " + this.methodToUse + " method to create "
					+ "agents but cannot convert " + this.definition + " into an integer.");
		}*/
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}
		
		LOGGER.info("Creating " + numAgents + " agents using Sanemori(" + this.methodToUse + ") method.");
		
		/** Choose 350 Random Houses as Homes **/
		int totalNumOfHomes = 350;
		int homesCreated = 0;
		int adultsCreated = 0;
		int totalNumAdults = 600;
		int totalChildrenCreated = 0;
		int teensCreated = 0;
		//set agendas
//		AgendaFactory adultAgenda = new AgendaFactory(0);
//		AgendaFactory childAgenda = new AgendaFactory(5);
//		AgendaFactory teenAgenda = new AgendaFactory(10);
		

		Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, totalNumOfHomes)
					.iterator();
			while (i.hasNext() && homesCreated < totalNumOfHomes) {
				/** 		**/
				/** need something here to make sure houses are unique (not repeating random) **/
				/** 		**/
				Building b = i.next(); // Find a building
				homesCreated++;
				/** Create Adult Agents & Assign to Houses **/
				/** ====================================== **/
				if (adultsCreated < 100) { // add 100 adults
					/** 100 Single Adults (1 per house) **/
					IAgent singlePerson = new DefaultAgent(); // Create a new agent
					singlePerson.setHome(b); // Tell the agent where it lives
					singlePerson.setType(0);

					b.addAgent(singlePerson); // Tell the building that the agent lives there
					ContextManager.addAgentToContext(singlePerson); // Add the agent to the context
					// Finally move the agent to the place where it lives.
					ContextManager.moveAgent(singlePerson, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					adultsCreated++;
				} else { //add the couples
					/** 500 Married Adults (2 per house) **/
					// Create a new agent
					IAgent coupleMama = new DefaultAgent(); 
					IAgent coupleDaddy = new DefaultAgent(); 
					
					// Tell the agent where it lives
					coupleMama.setHome(b); 
					coupleDaddy.setHome(b); 
					// Set Agent type
					coupleMama.setType(0);
					coupleDaddy.setType(0);
					
//					coupleMama.setAgenda(adultAgenda);
//					coupleDaddy.setAgenda(adultAgenda);
					
					//connect families
					coupleMama.setPartner(coupleDaddy);
					coupleDaddy.setPartner(coupleMama);
					
					// Tell the building that the agent lives there
					b.addAgent(coupleMama); 
					b.addAgent(coupleDaddy); 
					// Add the agent to the context
					ContextManager.addAgentToContext(coupleMama); 
					ContextManager.addAgentToContext(coupleDaddy); 
					// Finally move the agent to the place where it lives.
					ContextManager.moveAgent(coupleMama, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					ContextManager.moveAgent(coupleDaddy, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					adultsCreated = adultsCreated + 2;
					/** ADD CHILDREN **/
					if (totalChildrenCreated < 200) { 
						/** 100 couples with 2 children **/
						IAgent child1 = new DefaultAgent(); 
						IAgent child2 = new DefaultAgent(); 
						
						child1.setType(5); // child
						child2.setType(10); // teenager
						
						child1.setHome(b);
						child2.setHome(b);
						//connect families:
						child1.setFather(coupleDaddy);
						child1.setMother(coupleMama);
						child2.setFather(coupleDaddy);
						child2.setMother(coupleMama);
						
						coupleMama.setChild1(child1);
						coupleMama.setChild2(child2);
						coupleDaddy.setChild1(child1);
						coupleDaddy.setChild2(child2);
						
						child1.setSibling(child2);
						child2.setSibling(child1);

						// Tell the building that the agent lives there
						b.addAgent(child1); 
						b.addAgent(child2); 
						// Add the agent to the context
						ContextManager.addAgentToContext(child1); 
						ContextManager.addAgentToContext(child2); 
						// Finally move the agent to the place where it lives.
						ContextManager.moveAgent(child1, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						ContextManager.moveAgent(child2, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						
						teensCreated++;
						
						totalChildrenCreated = totalChildrenCreated + 2;
					} else if ((totalChildrenCreated >= 200) && (totalChildrenCreated < 400)) {
						/** 200 couples with 1 child **/
						IAgent child = new DefaultAgent(); 
						
						if (teensCreated < 200) {
							child.setType(10); //teenager
							//set agenda
//							child.setAgenda(teenAgenda);
							teensCreated++;
						} else {
							child.setType(5); //child
							//set agenda
//							child.setAgenda(childAgenda);
						}
						child.setHome(b);

						//connect families:
						child.setMother(coupleMama);
						child.setFather(coupleDaddy);

						coupleMama.setChild1(child);
						coupleDaddy.setChild1(child);
						
						
						b.addAgent(child); 
						ContextManager.addAgentToContext(child); 
						ContextManager.moveAgent(child, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						
						totalChildrenCreated++;
					}
				}
				

			} //end while i.hasNext() (buildings)
//		} // end while homesCreated < totalNumOfHomes
		LOGGER.info("DONE. Created " + adultsCreated + " adults and " + totalChildrenCreated + " children in " + homesCreated + " homes.");
		
	}
	
	private void createSanemoriTestAgents(boolean dummy) throws AgentCreationException {
		// Check the definition is as expected, in this case it should be a number

		int numAgents = -1;
/*
		try {
			numAgents = Integer.parseInt(this.definition);
		} catch (NumberFormatException ex) {
			throw new AgentCreationException("Using " + this.methodToUse + " method to create "
					+ "agents but cannot convert " + this.definition + " into an integer.");
		}*/
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}
		
		LOGGER.info("Creating " + numAgents + " agents using SanemoriTest(" + this.methodToUse + ") method.");
		
		/** Choose 350 Random Houses as Homes **/
		int totalNumOfHomes = 7;
		int homesCreated = 0;
		int adultsCreated = 0;
		int totalNumAdults = 7;
		int totalChildrenCreated = 0;
		int teensCreated = 0;
		int singleones = 0;
			Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, totalNumOfHomes)
					.iterator();
			while (i.hasNext() && homesCreated < totalNumOfHomes) {
				/** 		**/
				/** need something here to make sure houses are unique (not repeating random) **/
				/** 		**/
				Building b = i.next(); // Find a building
				homesCreated++;
				/** Create Adult Agents & Assign to Houses **/
				/** ====================================== **/
				if (adultsCreated < 3) { // add 100 adults
					/** 100 Single Adults (1 per house) **/
					IAgent singlePerson = new DefaultTestAgent();
					
					System.out.println("Agent "+singlePerson.getID()+ " is created as single adult");
					singlePerson.setHome(b); // Tell the agent where it lives
					singlePerson.setType(0);

					b.addAgent(singlePerson); // Tell the building that the agent lives there
					ContextManager.addAgentToContext(singlePerson); // Add the agent to the context
					// Finally move the agent to the place where it lives.
					ContextManager.moveAgent(singlePerson, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					adultsCreated++;
					singleones = adultsCreated;
					System.out.println(singleones+ " single agents created ");
				} else { //add the couples
					/** 500 Married Adults (2 per house) **/
					// Create a new agent
					IAgent coupleMama = new DefaultTestAgent();
					System.out.println("Agent "+coupleMama.getID()+ " is created as a mother");

					IAgent coupleDaddy = new DefaultTestAgent(); 
					System.out.println("Agent "+coupleDaddy.getID()+ " is created as a father");
					// Tell the agent where it lives
					coupleMama.setHome(b); 
					coupleDaddy.setHome(b); 
					// Set Agent type
					coupleMama.setType(0);
					coupleDaddy.setType(0);
					coupleMama.setPartner(coupleDaddy);
					coupleDaddy.setPartner(coupleMama);
					// Tell the building that the agent lives there
					b.addAgent(coupleMama); 
					b.addAgent(coupleDaddy); 
					// Add the agent to the context
					ContextManager.addAgentToContext(coupleMama); 
					ContextManager.addAgentToContext(coupleDaddy); 
					// Finally move the agent to the place where it lives.
					ContextManager.moveAgent(coupleMama, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					ContextManager.moveAgent(coupleDaddy, ContextManager.buildingProjection.getGeometry(b).getCentroid());
					adultsCreated = adultsCreated + 2;
					System.out.println((adultsCreated-singleones)+ " more agents created ");
					System.out.println(adultsCreated+ " total adult agents created ");

					/** ADD CHILDREN **/
					if (totalChildrenCreated < 4) { 
						/** 100 couples with 2 children **/
						IAgent child1 = new DefaultTestAgent(); 
						System.out.println("Agent "+child1.getID()+ " is created as a little kid!!");

						IAgent child2 = new DefaultTestAgent(); 
						System.out.println("Agent "+child2.getID()+ " is created as a teenager!!");

						child1.setSibling(child2);
						child2.setSibling(child1);
						child1.setType(5); // child
						child2.setType(10); // teenager
						
						child1.setHome(b);
						child2.setHome(b);
						
						child1.setMother(coupleMama);
						System.out.println("Agent "+coupleMama.getID()+ " is the mother of "+child1.getID());

						child1.setFather(coupleDaddy);
						System.out.println("Agent "+coupleDaddy.getID()+ " is the father of "+child1.getID());

						child2.setMother(coupleMama);
						System.out.println("Agent "+coupleMama.getID()+ " is the mother of "+child2.getID());

						child2.setFather(coupleDaddy);
						System.out.println("Agent "+coupleDaddy.getID()+ " is the mother of "+child2.getID());

						// Tell the building that the agent lives there
						b.addAgent(child1); 
						b.addAgent(child2); 
						// Add the agent to the context
						ContextManager.addAgentToContext(child1); 
						ContextManager.addAgentToContext(child2); 
						// Finally move the agent to the place where it lives.
						ContextManager.moveAgent(child1, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						ContextManager.moveAgent(child2, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						
						teensCreated++;
						System.out.println("Teenager count: "+teensCreated);
						
						System.out.println("Agent "+coupleDaddy.getID()+ " and Agent "+coupleDaddy.getPartner()+ "are a couple.. ");
						System.out.println("Their Children, Agent "+child1.getID()+ " and Agent "+child1.getSibling()+ " are siblings. ");



						totalChildrenCreated = totalChildrenCreated + 2;
					} else if ((totalChildrenCreated >= 4) && (totalChildrenCreated < 7)) {
						/** 200 couples with 1 child **/
						IAgent child = new DefaultTestAgent(); 
						System.out.println("Agent "+child.getID()+ " is created as a child since the total number of children is btwn 4-7");

						if (teensCreated < 2) {
							System.out.println("Agent "+child.getID()+ " is created as a teenager");
							System.out.println("because the number of teenagers are less than 2");

							child.setType(10); //teenager
							teensCreated++;
							System.out.println("Teenager count: "+teensCreated);

						} else {
							System.out.println("Agent "+child.getID()+ " is created as a little kid");
							child.setType(5); //child
						}
						child.setHome(b);

						child.setMother(coupleMama);
						System.out.println("Agent "+coupleMama.getID()+ " is the mother of "+child.getID());

						child.setFather(coupleDaddy);
						System.out.println("Agent "+coupleDaddy.getID()+ " is the father of "+child.getID());

						b.addAgent(child); 
						ContextManager.addAgentToContext(child); 
						ContextManager.moveAgent(child, ContextManager.buildingProjection.getGeometry(b).getCentroid());
						
						System.out.println("Agent "+coupleDaddy.getID()+ " and Agent "+coupleDaddy.getPartner()+ " are a couple.. ");

						
						totalChildrenCreated++;
						System.out.println("Total children count: "+totalChildrenCreated);

					}
				}
				

			} //end while i.hasNext() (buildings)
//		} // end while homesCreated < totalNumOfHomes
		LOGGER.info("DONE. Created " + adultsCreated + " adults and " + totalChildrenCreated + " children in " + homesCreated + " homes.");
		
	}
	
	/**
	 * Create a number of in randomly chosen houses. If there are more agents than houses then some houses will have
	 * more than one agent in them.
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then just check that the definition can be
	 *            parsed.
	 * @throws AgentCreationException
	 */
/*	private void createRandomAgents(boolean dummy) throws AgentCreationException {
		// Check the definition is as expected, in this case it should be a number
		int numAgents = -1;
		try {
			numAgents = Integer.parseInt(this.definition);
		} catch (NumberFormatException ex) {
			throw new AgentCreationException("Using " + this.methodToUse + " method to create "
					+ "agents but cannot convert " + this.definition + " into an integer.");
		}
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}

		// Create agents in randomly chosen houses. Use two while loops in case there are more agents
		// than houses, so that houses have to be looped over twice.
		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
		int agentsCreated = 0;
		while (agentsCreated < numAgents) {
			Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, numAgents)
					.iterator();
			while (i.hasNext() && agentsCreated < numAgents) {
				Building b = i.next(); // Find a building
				IAgent a = new DefaultAgent(); // Create a new agent
				a.setHome(b); // Tell the agent where it lives
				b.addAgent(a); // Tell the building that the agent lives there
				ContextManager.addAgentToContext(a); // Add the agent to the context
				// Finally move the agent to the place where it lives.
				ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
				agentsCreated++;
			}
		}
	}
*/
	/**
	 * Read a shapefile and create an agent at each location. If there is a column called
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then just check that the definition can be
	 *            parsed.
	 * @throws AgentCreationException
	 */
/*	@SuppressWarnings("unchecked")
	private void createPointAgents(boolean dummy) throws AgentCreationException {

		// See if there is a single type of agent to create or should read a colum in shapefile
		boolean singleType = this.definition.contains("$");

		String fileName;
		String className;
		Class<IAgent> clazz;
		if (singleType) {
			// Agent class provided, can use the Simphony Shapefile loader to load agents of the given class

			// Work out the file and class names from the agent definition
			String[] split = this.definition.split("\\$");
			if (split.length != 2) {
				throw new AgentCreationException("There is a problem with the agent definition, I should be "
						+ "able to split the definition into two parts on '$', but only split it into " + split.length
						+ ". The definition is: '" + this.definition + "'");
			}
			 // (Need to append root data directory to the filename).
			fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)+split[0];
			className = split[1];
			// Try to create a class from the given name.
			try {
				clazz = (Class<IAgent>) Class.forName(className);
				GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.getAgentGeography(), ContextManager
						.getAgentContext());
			} catch (Exception e) {
				throw new AgentCreationException(e);
			}
		} else {
			// TODO Implement agent creation from shapefile value;
			throw new AgentCreationException("Have not implemented the method of reading agent classes from a "
					+ "shapefile yet.");
		}

		// Assign agents to houses
		int numAgents = 0;
		for (IAgent a : ContextManager.getAllAgents()) {
			numAgents++;
			Geometry g = ContextManager.getAgentGeometry(a);
			for (Building b : SpatialIndexManager.search(ContextManager.buildingProjection, g)) {
				if (ContextManager.buildingProjection.getGeometry(b).contains(g)) {
					b.addAgent(a);
					a.setHome(b);
				}
			}
		}

		if (singleType) {
			LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file "
					+ fileName);
		} else {
			// (NOTE: at the moment this will never happen because not implemented yet.)
			LOGGER.info("Have created " + numAgents + " of different types from file " + fileName);
		}

	}
*/
	private void createAreaAgents(boolean dummy) throws AgentCreationException {
		throw new AgentCreationException("Have not implemented the createAreaAgents method yet.");
	}

	/**
	 * The methods that can be used to create agents. 
	 */
	private enum AGENT_FACTORY_METHODS {
		/** Default: create a number of agents randomly assigned to buildings */
		RANDOM("crawfish_epidemic", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
//				af.createRandomAgents(b);
				af.createSanemoriAgents(b);
			}
		}),
/*		SANEMORI("sanemori", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createSanemoriAgents(b);
			}
		}),
*/
		/** Specify an agent shapefile, one agent will be created per point */
	/*	POINT_FILE("point", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createPointAgents(b);
			}
		}),*/
		/**
		 * Specify the number of agents per area as a shapefile. Agents will be randomly assigned to houses within the
		 * area.
		 */
		AREA_FILE("test", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createSanemoriTestAgents(b);
			}
		});

		String stringVal;
		CreateAgentMethod meth;

		/**
		 * @param val
		 *            The string representation of the enum which must match the method given in the 'agent_definition'
		 *            parameter in parameters.xml.
		 * @param f
		 */
		AGENT_FACTORY_METHODS(String val, CreateAgentMethod f) {
			this.stringVal = val;
			this.meth = f;
		}

		public String toString() {
			return this.stringVal;
		}

		public CreateAgentMethod createAgMeth() {
			return this.meth;
		}

		interface CreateAgentMethod {
			void createagents(boolean dummy, AgentFactory af) throws AgentCreationException;
		}
	}

}
