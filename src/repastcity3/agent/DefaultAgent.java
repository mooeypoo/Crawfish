/*
©Copyright 2012 Nick Malleson
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
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
 */

package repastcity3.agent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import repast.simphony.random.RandomHelper;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	private IAgent mother = null;
	private IAgent father = null;
	private IAgent child1 = null;
	private IAgent child2 = null;
	private IAgent sibling = null;
	private IAgent partner = null;
	
	private double timeSpentInLocation = 0;

	private double timeGotIntoLocation = -1.0;
	/**
	 * Boolean stayHome
	 * false if going to work/school/kindergarden
	 * true if sick child, or if a parent of a sick child 
	 */
	private Boolean stayHome = false; //set true if parent of a sick child
	
	
	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Building mall; // random mall for recreation
	private Route route; // An object to move the agent around the world

	private Building currentBuilding; //the building the agent is currently located
	
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	/** 
	 * Agent Types:
	 * 0 - Adult
	 * 5 - Child
	 * 10 - Teenager
	 */
	private int agentType = 0;
	
	private DiseaseStages myHealthStatus = DiseaseStages.S;
	private static int counter =0;
	private static int uniqueID = 0;
	private int id;

	private boolean hasChildren =false;
//	private AgendaFactory myAgenda;
	
	public DefaultAgent() {
		this.setID(uniqueID++);
		this.workplace = createWorkplace();
	}

	@Override
	public void step() throws Exception {
		/** CHECK TIME OF DAY **/
		double theTime = BigDecimal.valueOf(ContextManager.realTime).
		        round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();

		/** Calling the Agenda item from 'step' causes the code to 
		 * break on "NullPointerException", probably because too many
		 * agents ask the same class too many requests. We need to solve this
		 * so we can have a single Agenda for all rather than configure agendas
		 * in the code itself.
		 * 
		 * But for the meantime, I'll use a function inside DefaultAgent
		 * (Look at 'getNextAgenda'
		 */
		Building nextDestBuilding = this.getNextAgendaItem(theTime);
		if (nextDestBuilding != null) {
			this.route = new Route(this, nextDestBuilding.getCoords(), nextDestBuilding); // Create a route to work
			this.currentBuilding = nextDestBuilding;
		}
		
		if (this.route == null) {
			/** 
			 * Null route means the agent is inside a building.
			 * 
			 * Start counting the time Agent is at location
			 * so we can use this for the infectiousness calculation
			 * when the agent leaves the location
			 *  **/
			this.timeSpentInLocation++;
			
		} else if (!this.route.atDestination()) {
			//Agent on the way
			this.route.travel();
		} else {
			//Agent reached destination. Delete the route:
			this.route = null;
		}

	} // step()

	/** 
	 * Calculate the infectiousness of whatever building
	 * the agent just left, to calculate the agent's odds
	 * of being infected
	 */
	public double calcInfectiousRate() {
		double result = 0;
		
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
				nextPlaceStr = "Work";
			}
			if(this.getType() == GlobalVars.P_CHILD){
				if(currTime == 18.0){
					nextPlace = this.home;
					nextPlaceStr = "Home";
				}
				
			}
			if (this.getType() == GlobalVars.P_ADULT) {
				if (currTime == 17.0) { // 17:00
					nextPlace = this.home;
					nextPlaceStr = "Home";
				} 
				if (this.isHasChildren() == false) {
					if (currTime == 19.5) { // 19:30
						nextPlace = findBuilding(GlobalVars.ACT_MALL);
						nextPlaceStr = "Mall";
					} else if (currTime == 23.0) { // 23:00
						nextPlace = this.home;
						nextPlaceStr = "Home";
					}
				}
			} else if (this.getType() == GlobalVars.P_TEEN) { 
				if (currTime == 15.00) {
					nextPlace = this.home;
					nextPlaceStr = "Home";
				}
			}
		}
		
		if (nextPlace == null) {
			return null;
		} else {
			System.out.println(currTime + " ["+this.getType()+"] Agent "+this.getID() + " --> " + nextPlaceStr);
			System.out.println("	> Time spent at previous location: " + timeSpentInLocation + "m");
			/** 
			 * Agent is leaving towards a new destination. 
			 * Make sure the time counter is back to zero
			 */

			this.timeSpentInLocation = 0;
			return nextPlace;
		}
	}
	
	private Building createWorkplace() {
		Building work = null;
		if (this.getType() == GlobalVars.P_ADULT) { //adult
			work = findBuilding(GlobalVars.ACT_WORK);
		} else if (this.getType() == GlobalVars.P_CHILD) { //child
			work = findBuilding(GlobalVars.ACT_KINDERGARTEN);
		} else if (this.getType() == GlobalVars.P_TEEN) { //teen
			work = findBuilding(GlobalVars.ACT_SCHOOL);
		}

		return work;
	}
	
	/** Find a random building by type **/
	private Building findBuilding(int buildingType) {
		Building build = null;
		for (Building b:ContextManager.buildingContext.getRandomObjects(Building.class, 10000)) {
			if (b.getType() == buildingType) { 
				build = b;
				break;
			}		
		}
		return build;
	}

	
	private boolean hasChildren() {
		if ((this.child1 != null) || (this.child2 != null)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void setType(int type) {
		this.agentType = type;
	}
	
	@Override
	public int getType() {
		return this.agentType;
	}

	@Override
	public void setMother(IAgent mom) {
		this.mother = mom;
	}

	@Override
	public void setFather(IAgent dad) {
		this.father = dad;
	}
	
	@Override
	public IAgent getMother() {
		return this.mother;
	}
	
	public IAgent getFather() {
		return this.father;
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
	public void setMyHealth(DiseaseStages st) {
		this.myHealthStatus = st;
	}
	
	@Override
	public DiseaseStages getMyHealth() {
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

	public void setID(int id) {
		this.id = id;
	}


	@Override
	public void setSibling(IAgent sibling) {
		this.sibling = sibling;
	}

	@Override
	public void setPartner(IAgent partner) {
		this.partner = partner;
	}

	@Override
	public IAgent getSibling() {
		return this.sibling;
	}

	@Override
	public IAgent getPartner() {
		return this.partner;
	}
	
	@Override
	public IAgent getChild1() {
		return this.child1;
	}

	@Override
	public void setChild1(IAgent child) {
		this.child1 = child;
	}
	@Override
	public IAgent getChild2() {
		return this.child2;
	}
	@Override
	public void setChild2(IAgent child) {
		this.child2 = child;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

}
