/*©Copyright 2012 Nick Malleson
This file is part of RepastCity.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

		GlobalVars.isDebug = true;
		
		int numAgents = -1;
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}
		
		LOGGER.info("Creating " + numAgents + " agents using Sanemori(" + this.methodToUse + ") method.");
		
		/** Choose 350 Random Houses as Homes **/
		int totalNumOfHomes = 350;
		int totalNumAdults = 600;
		int totalNumAdultsSingle = 100;
		
		int homesCreated = 0, adultsCreated = 0, teensCreated = 0;
		int totalChildrenCreated = 0;

		this.debugOutput("housetype,houseid,mother,father,child1,child2\n");
		
		
//		Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, totalNumOfHomes)
//					.iterator();
		List<Building> blist = getBuildingListbyType(1, totalNumOfHomes);
		Iterator<Building> i = blist.iterator();
			while (i.hasNext() && homesCreated <= totalNumOfHomes) {
				/** 		**/
				/** need something here to make sure houses are unique (not repeating random) **/
				/** 		**/
				Building b = i.next(); // Find a building
				homesCreated++;
				this.debugOutput(b.getType()+","+b.hashCode()+",");
				/** Create Adult Agents & Assign to Houses **/
				/** ====================================== **/
				if (GlobalVars.popListAdult.size() <= totalNumAdultsSingle) { 
					//add single adults
					/** 100 Single Adults (1 per house) **/
					IAgent person = new DefaultAgent(); // Create a new agent
					
					// Add to global population list:
					GlobalVars.popListAdult.add(person);
					int latestPersonIndex = GlobalVars.popListAdult.size()-1;
					// Set Person Variables:
					GlobalVars.popListAdult.get(latestPersonIndex).setID(latestPersonIndex);
					GlobalVars.popListAdult.get(latestPersonIndex).setHome(b);
					GlobalVars.popListAdult.get(latestPersonIndex).setType(GlobalVars.P_ADULT);

					// Tell the building that the agent lives there
					b.addAgent(GlobalVars.popListAdult.get(latestPersonIndex)); 

					// Get Person in Context:
					ContextManager.addAgentToContext(GlobalVars.popListAdult.get(latestPersonIndex)); // Add the agent to the context
					ContextManager.moveAgent(GlobalVars.popListAdult.get(latestPersonIndex), ContextManager.buildingProjection.getGeometry(b).getCentroid());
					
					this.debugOutput(latestPersonIndex + ",,,");
					
				} else {
					//Add the rest of the adults as couples
					
					/** MAMA **/
					IAgent pMama = new DefaultAgent(); // Create a new agent
					GlobalVars.popListAdult.add(pMama);
					int mamaIndex = GlobalVars.popListAdult.size()-1;
					/** DADDY **/
					IAgent pDaddy = new DefaultAgent(); // Create a new agent
					GlobalVars.popListAdult.add(pDaddy);
					int daddyIndex = GlobalVars.popListAdult.size()-1;
					
//					System.out.println("New Couple:");
//					System.out.println("	Mama, popListAdult id: " + mamaIndex);
//					System.out.println("	Daddy, popListAdult id: " + daddyIndex);

					// Set Person Variables:
					GlobalVars.popListAdult.get(mamaIndex).setHome(b);
					GlobalVars.popListAdult.get(mamaIndex).setID(mamaIndex);
					GlobalVars.popListAdult.get(mamaIndex).setType(GlobalVars.P_ADULT);
					
					GlobalVars.popListAdult.get(daddyIndex).setHome(b);
					GlobalVars.popListAdult.get(daddyIndex).setID(daddyIndex);
					GlobalVars.popListAdult.get(daddyIndex).setType(GlobalVars.P_ADULT);

					// Connect agents to their home:
					b.addAgent(GlobalVars.popListAdult.get(mamaIndex)); 
					b.addAgent(GlobalVars.popListAdult.get(daddyIndex)); 

					// Marriage:
					GlobalVars.popListAdult.get(mamaIndex).setPartner(daddyIndex);
					GlobalVars.popListAdult.get(daddyIndex).setPartner(mamaIndex);
					
					// Get Couple in Context:
					ContextManager.addAgentToContext(GlobalVars.popListAdult.get(mamaIndex)); // Add the agent to the context
					ContextManager.moveAgent(GlobalVars.popListAdult.get(mamaIndex), ContextManager.buildingProjection.getGeometry(b).getCentroid());
					ContextManager.addAgentToContext(GlobalVars.popListAdult.get(daddyIndex)); // Add the agent to the context
					ContextManager.moveAgent(GlobalVars.popListAdult.get(daddyIndex), ContextManager.buildingProjection.getGeometry(b).getCentroid());
					
					this.debugOutput(mamaIndex+","+daddyIndex+",");
					/** ADD OFFSPRING **/
					if (GlobalVars.popListChild.size() <= 200) {
							//Set the first 200 children as 1 child in household
							
							IAgent child = new DefaultAgent(); // Create a new agent
							child.setHome(b);
							child.setFather(daddyIndex);
							child.setMother(mamaIndex);
							
							int childIndex = -1;
							if (GlobalVars.popListChild.size() <= 100) {
								//child
								child.setType(GlobalVars.P_CHILD);
							} else {
								//teen
								child.setType(GlobalVars.P_TEEN);
							}
							// Add to global population list:
							GlobalVars.popListChild.add(child);
							childIndex = GlobalVars.popListChild.size()-1;

							//add child to parents:
							GlobalVars.popListAdult.get(mamaIndex).setChild1(childIndex);
							GlobalVars.popListAdult.get(daddyIndex).setChild1(childIndex);
	
							GlobalVars.popListChild.get(childIndex).setID(childIndex);
							// Tell the building that the agent lives there
							b.addAgent(GlobalVars.popListChild.get(childIndex)); 

							// Get Person in Context:
							ContextManager.addAgentToContext(GlobalVars.popListChild.get(childIndex)); // Add the agent to the context
							ContextManager.moveAgent(GlobalVars.popListChild.get(childIndex), ContextManager.buildingProjection.getGeometry(b).getCentroid());

							this.debugOutput(childIndex + "("+GlobalVars.popListChild.get(childIndex).getType()+"),");

							
					} else if ((GlobalVars.popListChild.size() > 200) && (GlobalVars.popListChild.size() <= 400)) { 
							//Set 200 children as 2 in household
							IAgent child1 = new DefaultAgent(); // Create a new agent
							IAgent child2 = new DefaultAgent(); // Create a new agent
							
							child1.setHome(b);
							child1.setFather(daddyIndex);
							child1.setMother(mamaIndex);
							child1.setType(GlobalVars.P_CHILD);
							
							GlobalVars.popListChild.add(child1);
							int child1Index = GlobalVars.popListChild.size()-1;
							
							child2.setHome(b);
							child2.setFather(daddyIndex);
							child2.setMother(mamaIndex);
							child2.setType(GlobalVars.P_TEEN);
							
							GlobalVars.popListChild.add(child2);
							int child2Index = GlobalVars.popListChild.size()-1;

	
							//add children to parents:
							GlobalVars.popListAdult.get(mamaIndex).setChild1(child1Index);
							GlobalVars.popListAdult.get(daddyIndex).setChild1(child1Index);
							GlobalVars.popListAdult.get(mamaIndex).setChild2(child2Index);
							GlobalVars.popListAdult.get(daddyIndex).setChild2(child2Index);
							GlobalVars.popListAdult.get(mamaIndex).setHasChildren(true);
							GlobalVars.popListAdult.get(daddyIndex).setHasChildren(true);
							
							GlobalVars.popListChild.get(child1Index).setSibling(child2Index);
							GlobalVars.popListChild.get(child2Index).setSibling(child1Index);
							
							GlobalVars.popListChild.get(child1Index).setID(child1Index);
							GlobalVars.popListChild.get(child2Index).setID(child2Index);
							// Tell the building that the agent lives there
							b.addAgent(GlobalVars.popListChild.get(child1Index)); 
							b.addAgent(GlobalVars.popListChild.get(child2Index)); 
	
							// Get Person in Context:
							ContextManager.addAgentToContext(GlobalVars.popListChild.get(child1Index)); // Add the agent to the context
							ContextManager.moveAgent(GlobalVars.popListChild.get(child1Index), ContextManager.buildingProjection.getGeometry(b).getCentroid());
	
							ContextManager.addAgentToContext(GlobalVars.popListChild.get(child2Index)); // Add the agent to the context
							ContextManager.moveAgent(GlobalVars.popListChild.get(child2Index), ContextManager.buildingProjection.getGeometry(b).getCentroid());
							this.debugOutput(child1Index + "("+GlobalVars.popListChild.get(child1Index).getType()+")," + child2Index + "("+GlobalVars.popListChild.get(child2Index).getType()+")");
					} else {
						this.debugOutput(",");
					}
					
				} // end if 'single adults'
				this.debugOutput("\n");
			} //end while i.hasNext() (buildings)
//		} // end while homesCreated < totalNumOfHomes
		LOGGER.info("DONE. Created " + GlobalVars.popListAdult.size() + " adults and " + GlobalVars.popListChild.size() + " children in " + homesCreated + " homes.");	
	}

	
	/*** I HAD TO COMMENT OUT OUR TEST 
	 * there were too many errors. We'll rewrite it later  */
/*	
	private void createSanemoriTestAgents(boolean dummy) throws AgentCreationException {
		// Check the definition is as expected, in this case it should be a number

		int numAgents = -1;
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}
		
		LOGGER.info("Creating " + numAgents + " agents using SanemoriTest(" + this.methodToUse + ") method.");
		
		/** Choose 350 Random Houses as Homes 
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
				/** 		**
				/** need something here to make sure houses are unique (not repeating random) **/
				/** 		
				Building b = i.next(); // Find a building
				homesCreated++;
				/** Create Adult Agents & Assign to Houses *
				/** ====================================== *
				if (adultsCreated < 3) { // add 100 adults
					/** 100 Single Adults (1 per house) **
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
					/** 500 Married Adults (2 per house) **
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

					/** ADD CHILDREN **
					if (totalChildrenCreated < 4) { 
						/** 100 couples with 2 children **
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
						coupleMama.setHasChildren(true);
						System.out.println("Agent "+coupleMama.getID()+ " is the mother of "+child1.getID());

						child1.setFather(coupleDaddy);
						System.out.println("Agent "+coupleDaddy.getID()+ " is the father of "+child1.getID());
						coupleDaddy.setHasChildren(true);
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
						/** 200 couples with 1 child **
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
						coupleMama.setHasChildren(true);

						child.setFather(coupleDaddy);
						System.out.println("Agent "+coupleDaddy.getID()+ " is the father of "+child.getID());
						coupleDaddy.setHasChildren(true);

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

*/	
	
	
	
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
//				af.createSanemoriTestAgents(b);
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
	
	public List<Building> getBuildingListbyType(int type, int numOfItems) {
		List<Building> bList = new ArrayList<Building>();
		int counter=0;
		Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, 10000)
				.iterator();
		while ((i.hasNext()) && (counter <= numOfItems)) {
			Building b = i.next();
			if (b.getType() == type) {
				bList.add(b);
				counter++;
			}
		}
		
		return bList;
	}
	
	public void debugOutput(String out) {
		if (GlobalVars.isDebug) {
			System.out.print(out);
		}
	}

}
