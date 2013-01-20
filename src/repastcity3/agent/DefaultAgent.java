/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 **/

package repastcity3.agent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.random.RandomHelper;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	
	/* FAMILY */
	private int mother = -1;
	private int father = -1;
	private int child1 = -1;
	private int child2 = -1;
	private int sibling = -1;
	private int partner = -1;
	
	private double timeSpentInLocation = 0;

	private double timeGotIntoLocation = -1.0;
	/**
	 * Boolean stayHome
	 * false if going to work/school/kindergarden
	 * true if sick child, or if a parent of a sick child 
	 */
	private Boolean stayHome = false; //set true if parent of a sick child
	
	private List<Building> nearbyBuildings = new ArrayList<Building>();;

	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Building mall; // random mall for recreation
	private Route route; // An object to move the agent around the world

	private Building currentBuilding; //the building the agent is currently located
	private boolean alreadyUpdatedBuilding = false;
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	private int agentType = 0;
	
	private DiseaseStages myHealthStatus = DiseaseStages.S;
	private static int counter =0;
	private static int uniqueID = 0;
	private int id;

	private boolean hasChildren =false;
//	private AgendaFactory myAgenda;
	
	public DefaultAgent() {
//		this.workplace = createWorkplace();
	}

	@Override
	public void step() throws Exception {

		/** CHECK TIME OF DAY **/
		double theTime = BigDecimal.valueOf(ContextManager.realTime).
		        round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();

		//to save calculation time, only check agenda items on 60-min intervals:
		if ((theTime%0.5) == 0) {
			Building nextDestBuilding = this.getNextAgendaItem(theTime);
			if ((nextDestBuilding != null)) { 
				// Leaving towards new destination.
				this.alreadyUpdatedBuilding = false;
				this.route = new Route(this, nextDestBuilding.getCoords(), nextDestBuilding); // Create a route to work
				this.currentBuilding = nextDestBuilding;
			}
		}
		
		/** ROUTE DEFINITIONS **/
		if (this.route == null) {
			/** 
			 * Null route means the agent is inside a building.
			 * 
			 * Start counting the time Agent is at location
			 * so we can use this for the infectiousness calculation
			 * when the agent leaves the location
			 *  **/
			this.timeSpentInLocation++;
			//for the first time, check the building (agent in):
			if (this.alreadyUpdatedBuilding == false) {
				//agentIn
				visitBuilding(true);
				double inf = 0;
//				synchronized (ContextManager.randomLock) {
//					inf = this.currentBuilding.getAgentsInHouse();
//					System.out.println(">>> Agent #"+this.getID()+": "+inf);
//				}
				this.alreadyUpdatedBuilding = true;
			}
		} else if (!this.route.atDestination()) {
			//Agent is traveling
			//for the first time, check the building (agent out)
			if (this.alreadyUpdatedBuilding==false) {
				visitBuilding(false);
				double inf = 0;
//				synchronized (ContextManager.randomLock) {
//					inf = this.currentBuilding.getAgentsInHouse();
//					System.out.println(">>> Agent #"+this.getID()+": "+inf);
//				}
				this.alreadyUpdatedBuilding = true;
			}
				

			this.route.travel();			

		} else if (this.route.atDestination()) {
			//Agent reached destination. 
			this.nearbyBuildings = this.route.getPassedBuildings();
			this.timeSpentInLocation = 0;
			this.alreadyUpdatedBuilding = false;
			this.route = null;
		}

	} // step()

	public boolean visitBuilding(boolean amIGoingIn) {
		synchronized (ContextManager.randomLock) {
			Iterator<Building> bList = ContextManager.buildingContext.getRandomObjects(Building.class, 10000).iterator();
			String msg = "";
			while (bList.hasNext()) {
				Building bld = bList.next();
				if (bld.equals(currentBuilding)) {
					if (amIGoingIn==true) {
						msg = "IN";
						bld.agentIn(this.getHealthStatus().isInfectious());
						
					} else {
						msg = "OUT";
						bld.agentOut(this.getHealthStatus().isInfectious());
					}
					System.out.println("Agent "+this.getID()+": GOING "+msg);
					return true;
				}
			}
		}
		return false;
	}

	/** 
	 * Calculate the infectiousness of whatever building
	 * the agent just left, to calculate the agent's odds
	 * of being infected
	 */
	public double calcInfectiousness(Building b) {
		double result = 0;
		//count how many S/I people are in the building:
		int allAgentsInBuilding=0, infectedAgentsInBuilding=0;
		
			allAgentsInBuilding = b.getAgentsInHouse();
			infectedAgentsInBuilding = b.getInfected();
		
		//count time
		if (allAgentsInBuilding > 0) {
			// the formula makes no sense.. I adapted it just to test, but
			// we need to figure it out... (talk to me i'll explain)
			result = GlobalVars.InfectionFactor * (infectedAgentsInBuilding * this.timeSpentInLocation) / allAgentsInBuilding;
		}
		return result;
	}

	
	/**
	 * Find the next building to go to based on person's
	 * agenda. Temporarily replace AgendaFactory
	 * 
	 * @return Building Next building to route to
	 */
	private Building getNextAgendaItem(double currTime) {
		Building nextPlace = null;
		String nextPlaceStr = "";
		if (this.stayHome == false) {
			// EVERYONE GOES TO WORK/KINDERGARDEN/SCHOOL 
			if (currTime == 9.0) { // 09:00
				nextPlace = this.workplace;
				if (this.getType() == GlobalVars.P_ADULT) {
					nextPlaceStr = "Work " + nextPlace.getType();
				} else if (this.getType() == GlobalVars.P_TEEN) {
					nextPlaceStr = "School " + nextPlace.getType();
				} else if (this.getType() == GlobalVars.P_CHILD) {
					nextPlaceStr = "Kindergarten " + nextPlace.getType();
				}
			}
			
			if(this.getType() == GlobalVars.P_CHILD){
				if(currTime == 18.0){
					nextPlace = this.home;
					nextPlaceStr = "Home";
				}
				
			} else if (this.getType() == GlobalVars.P_ADULT) {
				if (currTime == 17.0) { // 17:00
					nextPlace = this.home;
					nextPlaceStr = "Home ";
				} 
				if (this.isHasChildren() == false) {
					if (currTime == 20.0) { // 20:00
						nextPlace = findBuilding(GlobalVars.ACT_MALL);
						nextPlaceStr = "Evening Drinks!" + nextPlace.getType();
					} else if (currTime == 23.0) { // 23:00
						nextPlace = this.home;
						nextPlaceStr = "Home";
					}
				}
			} else if (this.getType() == GlobalVars.P_TEEN) { 
				if (currTime == 15.00) {
					nextPlace = this.home;
					nextPlaceStr = "Home" + nextPlace.getType();
				}
			}
		} else { // stayhome == true
/*			// send member home only if they're not already there:
			if (!this.route.getDestinationBuilding().equals(this.home)) { 
				nextPlace = this.home;
				nextPlaceStr = "Home (stay home)";
			}*/
		}
		
		if (nextPlace == null) {
			return null;
		} else {
//			System.out.println(currTime + " ["+this.getType()+"] Agent "+this.getID() + " --> " + nextPlaceStr);
			//double realTimeTranslation = (timeSpentInLocation/3)*60;
//			System.out.println("	> Time spent at previous location: " + timeSpentInLocation); // + " ticks ("+realTimeTranslation+" h)");
			/** 
			 * Agent is leaving towards a new destination. 
			 * Make sure the time counter is back to zero
			 */

			//this.timeSpentInLocation = 0;
			this.alreadyUpdatedBuilding=false;
			return nextPlace;
		}
	}
	
	@Override
//	public Building createWorkplace() {
	public void createWorkplace() {
		Building work = null;
		if (this.getType() == GlobalVars.P_ADULT) { //adult
			work = findBuilding(GlobalVars.ACT_WORK);
		} else if (this.getType() == GlobalVars.P_CHILD) { //child
			work = findBuilding(GlobalVars.ACT_KINDERGARTEN);
		} else if (this.getType() == GlobalVars.P_TEEN) { //teen
			work = findBuilding(GlobalVars.ACT_SCHOOL);
		}

		this.workplace = work;
	}
	
	@Override
	public Building getWorkplace() {
		return this.workplace;
	}
	
	/** Find a random building by type **/
	private Building findBuilding(int buildingType) {
		Building build = null;
		while (build == null) {
			for (Building b:ContextManager.buildingContext.getRandomObjects(Building.class, 10000)) {
				if (b.getType() == buildingType) { 
					build = b;
					break;
				}		
			}
		}
		return build;
	}

	
	public Building getCurrentBuilding() {
		return this.currentBuilding;
	}
	
	@Override
	public void setType(int type) {
		this.agentType = type;
	}
	
	@Override
	public int getType() {
		return this.agentType;
	}

	/**
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
//		return true;
		return false;
	}

	@Override
	public void setHome(Building home) {
		this.home = home;
	}

	
	@Override
	public Building getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Agent " + this.getID();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
		return this.getID() == b.getID();
	}

	@Override
	public int hashCode() {
		return this.getID();
	}
	
	@Override
	public void setHealthStatus(DiseaseStages st) {
		this.myHealthStatus = st;
		
		if (st.isSymptomatic() == true) {
			if (this.getType() == GlobalVars.P_CHILD) {
				this.stayHome = true;
				//randomly pick a parent to stay with the child:
				Random randomGenerator = new Random(123987);
				int rand = randomGenerator.nextInt(2);
				int parentIndex = -1;
				if (rand == 1) {
					parentIndex = this.getMother();
				} else {
					parentIndex = this.getFather();
				}
				GlobalVars.popListAdult.get(parentIndex).setStayHome(true);
			} else if (this.getType() == GlobalVars.P_CHILD) {
				this.stayHome = true;
			} else {
				//don't do anything for adults
			}
		} else {
			if (this.getType() == GlobalVars.P_CHILD) {
				this.stayHome = false;
				//let the parent go back to work (unless sibling is sick):
				if ((GlobalVars.popListChild.get(this.getSibling()).getType() == GlobalVars.P_CHILD) && (GlobalVars.popListChild.get(this.getSibling()).getHealthStatus().isSymptomatic()==true)) {
					//do nothing, sibling is a child who's sick
				} else {
					GlobalVars.popListAdult.get(this.getMother()).setStayHome(false);
					GlobalVars.popListAdult.get(this.getFather()).setStayHome(false);
				}
			} else if (this.getType() == GlobalVars.P_TEEN) {
				this.stayHome = false;
			} else {
				//do nothing for adults
			}
		}
	}
	
	@Override
	public DiseaseStages getHealthStatus() {
		return this.myHealthStatus;
	}

	@Override
	public void setStayHome(Boolean yesno) {
		this.stayHome = yesno;
	}

	@Override
	public Boolean getStayHome() {
		return this.stayHome;
	}
	
	@Override
	public int getID() {
		return id;
	}

	@Override
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * FAMILY METHODS
	 */
	@Override
	public void setPartner(int pIndex) {
		this.partner = pIndex;
	}
	
	@Override
	public int getPartner() {
		return this.partner;
	}
	
	@Override
	public void setFather(int pIndex) {
		this.father = pIndex;
	}
	
	@Override
	public int getFather() {
		return this.father;
	}

	@Override
	public void setMother(int pIndex) {
		this.mother = pIndex;
	}
	
	@Override
	public int getMother() {
		return this.mother;
	}


	
	@Override
	public void setSibling(int pIndex) {
		this.sibling = pIndex;
	}
	
	@Override
	public int getSibling() {
		return this.sibling;
	}

	@Override
	public void setChild1(int pIndex) {
		this.child1 = pIndex;
	}
	
	@Override
	public int getChild1() {
		return this.child1;
	}

	@Override
	public void setChildren(int c1, int c2) {
		setChild1(c1);
		setChild2(c2);
		this.setHasChildren(true);
	}
	
	@Override
	public void setChild2(int pIndex) {
		this.child2 = pIndex;
	}
	
	@Override
	public int getChild2() {
		return this.child2;
	}
	
	
	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	
	public void debugOutput(String out) {
		if (GlobalVars.isDebug) {
			System.out.print(out);
		}
	}


}
