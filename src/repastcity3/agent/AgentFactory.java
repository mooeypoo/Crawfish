/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 * AgentFactory
 * Creates the agents in the simulation and assigns them
 * to households as per the initial user-defined parameters
 * 
 **/

package repastcity3.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repastcity3.agent.IAgent.DiseaseStages;
import repastcity3.environment.Building;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;
import repastcity3.main.MODEL_PARAMETERS;


public class AgentFactory {

	private static Logger LOGGER = Logger.getLogger(AgentFactory.class.getName());

	/** WE SHOULD ADD THESE TO THE PARAMETERS!!! **/
	public static final int TOTAL_POPULATION_NUMBER = 200; // This will be approximate (close enough)

	private static final double PERC_ADULTS_MARRIED = 0.85; // 85% (out of all adults)
	private static final double PERC_ADULTS_WCHILD = 0.8; 	// 80% (out of married adults)
	
	private int initInfected_Adults 	= 1000;	//GlobalVars.INITIAL_INFECTED_ADULTS;
	private int initInfected_Teens 		= 500;	//GlobalVars.INITIAL_INFECTED_TEENS;
	private int initInfected_Children 	= 500; 	//GlobalVars.INITIAL_INFECTED_CHILDREN;
	/** ---------------------------------------- **/
	
	
	/** The method to use when creating agents (determined in constructor). */
	private AGENT_FACTORY_METHODS methodToUse;
	

	/** The definition of the agents - specific to the method being used */
	//private String definition;

	/**
	 * Create a new agent factory from the given definition.
	 * 
	 * @param agentDefinition
	 */
	public AgentFactory(String agentDefinition) throws AgentCreationException {

		String method = agentDefinition;

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
		double tempFix = (TOTAL_POPULATION_NUMBER * 0.25);
		int totalAdults = (int) (TOTAL_POPULATION_NUMBER - tempFix);
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
		
		this.debugOutput("building_type,building_id,parent1_id,parent2_id,child1_id,child2_id\n");

		List<Building> blist = getBuildingListbyType(1, totalNumOfHomes);
		Iterator<Building> i = blist.iterator();
		while (i.hasNext() && count_Homes <= totalNumOfHomes) {
			Building b = i.next();
			if (count_people <= adultsSingle) {
				// Single People (25%)
				int personIndex = this.addAdult(b);
				this.debugOutput(b.getType()+","+b.hashCode()+","+personIndex+",,,");
				count_people++;
			} else {//else if ((count_people > adultsSingle) && (count_people <= adultsCouple)) {
				// Married People (75%), 2 at a time
				int momIndex = this.addAdult(b);
				int dadIndex = this.addAdult(b);
				
				GlobalVars.popListAdult.get(momIndex).setPartner(dadIndex);
				GlobalVars.popListAdult.get(dadIndex).setPartner(momIndex);

				this.addPersonToContext(GlobalVars.popListAdult.get(momIndex), b);
				this.addPersonToContext(GlobalVars.popListAdult.get(dadIndex), b);
				this.debugOutput(b.getType()+","+b.hashCode()+","+momIndex+","+dadIndex+",");

				/** ADD CHILDREN **/
				if (count_people < (adultsSingle + adultsWithChild)) { //with child
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
						this.debugOutput(childIndex+",");
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
						this.debugOutput(child1Index+","+child2Index);
						count_Children = count_Children + 2;
					}
					count_WithChild = count_WithChild + 2;
				}
				
				count_people = count_people + 2;
			}
			
			count_Homes++;
			this.debugOutput("\n");
		}
		
		//Make initial infected:
		List<Integer> inf_Adult = getRandomAgents(GlobalVars.P_ADULT,initInfected_Adults);
		List<Integer> inf_Teen = getRandomAgents(GlobalVars.P_TEEN,initInfected_Teens);
		List<Integer> inf_Child = getRandomAgents(GlobalVars.P_CHILD,initInfected_Children);
		int infectedCounter = 0, infAcounter=0, infTcounter=0, infCcounter=0;
		
		if (inf_Adult.size() > 0) {
			for (int ind=0; ind<inf_Adult.size(); ind++) {			
				if (ind<GlobalVars.popListAdult.size() && GlobalVars.popListAdult.get(ind) != null) {
					GlobalVars.popListAdult.get(ind).setHealthStatus(DiseaseStages.I);
					infectedCounter++;
					infAcounter++;
				}
			}
		}
		if (inf_Teen.size() > 0) {
			for (int ind=0; ind<inf_Teen.size(); ind++) {
				if (ind<GlobalVars.popListChild.size() && GlobalVars.popListChild.get(ind) != null) {
					GlobalVars.popListChild.get(ind).setHealthStatus(DiseaseStages.I);
					infectedCounter++;
					infTcounter++;
				}
			}
		}
		if (inf_Child.size() > 0) {
			for (int ind=0; ind<inf_Child.size(); ind++) {
				if (ind<GlobalVars.popListChild.size() && GlobalVars.popListChild.get(ind) != null) {
					GlobalVars.popListChild.get(ind).setHealthStatus(DiseaseStages.I);
					infectedCounter++;
					infCcounter++;
				}
			}
		}
//		Iterator<Building> i = pList.iterator();

		
		LOGGER.info("Done. Created " + count_people + " adults, and " + count_Children + " children.");
		LOGGER.info("There are "+infectedCounter+" initial infected: "+infAcounter+" Adults, "+infCcounter+ " Children, "+infTcounter +" Teens.");
		
	}



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
	
	public List<Integer> getRandomAgents(int pType, int numOfRandElements) {
		List<Integer> pList = new ArrayList<Integer>();
		
		if (pType == GlobalVars.P_ADULT) {
			for (int i=0; i < numOfRandElements; i++) {
				Random randomGenerator = new Random(123987);
				int randInd = randomGenerator.nextInt(GlobalVars.popListAdult.size() - 1);
				pList.add(randInd);
			}
		} else {
			Random randomGenerator = new Random(123987);
			int counter = 0;
			while (counter < numOfRandElements) {
				int randInd = randomGenerator.nextInt(GlobalVars.popListChild.size() - 1);
				if (GlobalVars.popListChild.get(randInd).getType() == pType) {
					pList.add(randInd);
					counter++;
				}
			}
		}
		return pList;
	}
	
	/** Add Person to List **/
	private static int addAdult(Building pHome){
		int index = _addPerson(GlobalVars.P_ADULT, pHome);

		if (index > -1) {
			pHome.addAgent(GlobalVars.popListAdult.get(index));
			pHome.agentIn(GlobalVars.popListAdult.get(index).getID(), false);
		}
		return index;
	}
	
	private static int addChild(int pType, Building pHome, int pMother, int pFather) {
		int index = _addPerson(pType, pHome);
		if (index > -1) {
			GlobalVars.popListChild.get(index).setMother(pMother);
			GlobalVars.popListChild.get(index).setFather(pFather);
			pHome.addAgent(GlobalVars.popListChild.get(index));
			pHome.agentIn(GlobalVars.popListChild.get(index).getID(), false);
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
		person.createWorkplace();
		
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
