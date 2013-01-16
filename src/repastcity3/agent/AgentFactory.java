/*©Copyright 2012 Nick Malleson
This file is part of RepastCity.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

	private static final double PERC_ADULTS_MARRIED = 0.75;
	private static final double PERC_ADULTS_WCHILD = 0.8;
	
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
		
		
		/** PREPARE TO GIVE THE OPTION OF HOW MANY PEOPLE **/
		int totalAdults = 1000; //GlobalVars.NUM_OF_ADULTS; // THIS WILL BE GIVEN AS A PARAM
		/** **************************************************** **/
		LOGGER.info("Creating " + totalAdults + " agents using Sanemori(" + this.methodToUse + ") method.");
		
		int adultsCouple = (int) Math.round(totalAdults * this.PERC_ADULTS_MARRIED);
		int adultsSingle = totalAdults - adultsCouple;
		int adultsWithChild = (int) Math.round(adultsCouple * this.PERC_ADULTS_WCHILD);
		int adultsNoChild = adultsCouple - adultsWithChild;
		
		int totalNumOfHomes = (int) Math.round(adultsSingle + ((adultsWithChild + adultsNoChild) / 2));

		//counters:
		int count_people=0, count_WithChild=0, count_Children=0;
		int count_Homes=0;
		
		List<Building> blist = getBuildingListbyType(1, totalNumOfHomes);
		Iterator<Building> i = blist.iterator();
		while (i.hasNext() && count_Homes <= totalNumOfHomes) {
			Building b = i.next();
			if (count_people <= adultsSingle) {
				// Single People (25%)
				int personIndex = this.addAdult(b);
				count_people++;
			} else if ((count_people > adultsSingle) && (count_people <= adultsCouple)) {
				// Married People (75%), 2 at a time
				int momIndex = this.addAdult(b);
				int dadIndex = this.addAdult(b);
				
				GlobalVars.popListAdult.get(momIndex).setPartner(dadIndex);
				GlobalVars.popListAdult.get(dadIndex).setPartner(momIndex);

				this.addPersonToContext(GlobalVars.popListAdult.get(momIndex), b);
				this.addPersonToContext(GlobalVars.popListAdult.get(dadIndex), b);

				/** ADD CHILDREN **/
				if (count_people <= (adultsSingle + adultsWithChild)) { //with child
					if (count_WithChild <= (adultsWithChild/2)) {
						// ONE CHILD
						Random randomGenerator = new Random(123987);
						int rand = randomGenerator.nextInt(100);
						int childType = GlobalVars.P_TEEN;
						if (rand > 50) {
							childType = GlobalVars.P_CHILD;
						} 
						int childIndex = this.addChild(childType, b, momIndex, dadIndex);
						GlobalVars.popListAdult.get(momIndex).setChildren(childIndex, -1);
						GlobalVars.popListAdult.get(dadIndex).setChildren(childIndex, -1);

						this.addPersonToContext(GlobalVars.popListChild.get(childIndex), b);
						count_Children++;
					} else {
						// TWO CHILDREN
						int child1Index = this.addChild(GlobalVars.P_TEEN, b, momIndex, dadIndex);
						int child2Index = this.addChild(GlobalVars.P_CHILD, b, momIndex, dadIndex);
						
						GlobalVars.popListChild.get(child1Index).setSibling(child2Index);
						GlobalVars.popListChild.get(child2Index).setSibling(child1Index);

						GlobalVars.popListAdult.get(momIndex).setChildren(child1Index, child2Index);
						GlobalVars.popListAdult.get(dadIndex).setChildren(child1Index, child2Index);
						
						this.addPersonToContext(GlobalVars.popListChild.get(child1Index), b);
						this.addPersonToContext(GlobalVars.popListChild.get(child2Index), b);
						count_Children = count_Children + 2;
					}
					count_WithChild = count_WithChild + 2;
				}
				
				count_people = count_people + 2;
			}
			
			count_Homes++;
		}
		LOGGER.info("Done. Created " + count_people + " adults, and " + count_Children + " children.");
		
		
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
	
	/** Add Person to List **/
	private static int addAdult(Building pHome){
		int index = _addPerson(GlobalVars.P_ADULT, pHome);

		if (index > -1) {
			pHome.addAgent(GlobalVars.popListAdult.get(index));
		}
		return index;
	}
	
	private static int addChild(int pType, Building pHome, int pMother, int pFather) {
		int index = _addPerson(pType, pHome);
		if (index > -1) {
			GlobalVars.popListChild.get(index).setMother(pMother);
			GlobalVars.popListChild.get(index).setFather(pFather);
			pHome.addAgent(GlobalVars.popListAdult.get(index));
		}
		return index;
	}
	
	
	private static boolean addPersonToContext(IAgent person, Building b) {
		boolean result = false;
		try {
			ContextManager.addAgentToContext(person); // Add the agent to the context
			ContextManager.moveAgent(person, ContextManager.buildingProjection.getGeometry(b).getCentroid());
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
		
	}

	private static int _addPerson(int pType, Building pHome) {
		int index = -1;
		if (pType == GlobalVars.P_ADULT) {
			index = GlobalVars.popListAdult.size();
		} else {
			index = GlobalVars.popListChild.size();
		}

		IAgent person = new DefaultAgent(); // Create a new agent
		person.setType(pType);
		
		person.setHome(pHome);
		
		person.setID(index);
		
		try {
			if (pType == GlobalVars.P_ADULT) {
				GlobalVars.popListAdult.add(person);
			} else {
				GlobalVars.popListChild.add(person);
			}
		} catch (Exception e) {
			index = -1;
		}
		return index;
	}
	/** END ADD PERSON TO LIST **/

	
	public void debugOutput(String out) {
		if (GlobalVars.isDebug) {
			System.out.print(out);
		}
	}

}
