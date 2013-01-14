
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

public class DefaultTestAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultTestAgent.class.getName());

	private IAgent mother = null;
	private IAgent father = null;
	private IAgent child1 = null;
	private IAgent child2 = null;
	private IAgent sibling = null;
	private IAgent partner = null;
	
	private boolean hasChildren = false;
	

	private double timeSpentInLocation = 0;

	private double timeGotIntoLocation = -1.0;
	
	/**
	 * Boolean stayHome
	 * false if going to work/school/kindergarden
	 * true if sick child, or if a parent of a sick child 
	 */
	private Boolean stayHome = false; //set true if parent of a sick child
//	private AgendaFactory myAgenda;

	
	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Building mall; // random mall for recreation
	private Route route; // An object to move the agent around the world

	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	/** 
	 * Agent Types:
	 * 0 - Adult
	 * 5 - Child
	 * 10 - Teenager
	 */
	private int agentType = 0;
	
	private DiseaseStages myHealthStatus = DiseaseStages.S;
	
	private static int uniqueID = 0;
	private int id;

	public DefaultTestAgent() {
		this.id = uniqueID++;
		this.workplace = createWorkplace();

	}

	private Building createWorkplace() {
		// TODO Auto-generated method stub
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


	
	@Override
	public void step() throws Exception {
		/** CHECK TIME OF DAY **/
		double theTime = BigDecimal.valueOf(ContextManager.realTime).
		        round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();
		Building nextDestBuilding = this.getNextAgendaItem(theTime);
		if (nextDestBuilding != null) {
			this.route = new Route(this, nextDestBuilding.getCoords(), nextDestBuilding); // Create a route to work
		}
		
		if (this.route == null) {
			
		} else if (!this.route.atDestination()) {
			//Agent on the way
			this.route.travel();
		} else {
			//Agent reached destination:
			/** 
			 * Start counting the time Agent is at location
			 * so we can use this for the infectiousness calculation
			 * when the agent leaves the location
			 *  **/
			this.timeSpentInLocation++;
		}

	} // step()

	private Building getNextAgendaItem(double currTime) {
		// TODO Auto-generated method stub
		Building nextPlace = null;
		String nextPlaceStr = "";
		if (this.stayHome == false) {
			// EVERYONE GOES TO WORK/KINDERGARDEN/SCHOOL 
			if (currTime == 9.0) { // 09:00
				nextPlace = this.workplace;
				if(this.getType() == 0){
					nextPlaceStr = "Work";
				}else if(this.getType() == 5){
					nextPlaceStr = "Kindergarden";		
				}else{
					nextPlaceStr = "School";					
				}
				
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

	
	
	private Building findBuilding(int buildingType) {
		// TODO Auto-generated method stub
		Building build = null;
		for (Building b:ContextManager.buildingContext.getRandomObjects(Building.class, 10000)) {
			if (b.getType() == buildingType) { 
				build = b;
				break;
			}		
		}
		return build;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
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
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultTestAgent))
			return false;
		DefaultTestAgent b = (DefaultTestAgent) obj;
		return this.id == b.getID();
	}



	@Override
	public int hashCode() {
		return this.id;
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
		// TODO Auto-generated method stub
		return this.id;
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

/*
	@Override
	public void setAgenda(AgendaFactory agenda) {
		this.myAgenda = agenda;
	}
	
	@Override
	public AgendaFactory getAgenda() {
		return this.myAgenda;
	}
*/

}
