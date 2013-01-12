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

	private IAgent mother;
	private IAgent father;
	private IAgent sibling;
	private IAgent partner;
	
	/**
	 * Boolean stayHome
	 * false if going to work/school/kindergarden
	 * true if sick child, or if a parent of a sick child 
	 */
	private Boolean stayHome = false; //set true if parent of a sick child
	
	
	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
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
	private static int counter =0;
	private static int uniqueID = 0;
	private int id;
//	private AgendaFactory myAgenda;
	
	public DefaultAgent() {
		this.setID(uniqueID++);

	}

	@Override
	public void step() throws Exception {
		/** CHECK TIME OF DAY **/
		double theTime = BigDecimal.valueOf(ContextManager.realTime).
		        round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();

/*		
		if (this.myAgenda.getItem(theTime) != null) {
			LOGGER.info("["+theTime+ "] ("+this.getType()+") Agent " + this.getID() + " has an agenda item: " + myAgenda.getItem(theTime));
//			Building b = ContextManager.buildingContext.getRandomObject();
//			this.route = new Route(this, b.getCoords(), b);

			this.counter++;
			if (this.counter > 10) {
				System.exit(0);
			}
		}
*/
/*		
		if (theTime == 9.0) { // 9am, Agent should be leaving for work
//			this.route = new Route(this, this.workplace.getCoords(), this.workplace); // Create a route to work

			// route can only be null when the simulation starts, so the agent must be leaving home
			this.goingHome = false; // Choose a new building to go to
			Building b = ContextManager.buildingContext.getRandomObject();
			this.route = new Route(this, b.getCoords(), b);

		} else if (theTime == 17.0) { // 5pm, agent should go home
		//	this.route = new Route(this, this.home.getCoords(), this.home); // Create a route home
			// route can only be null when the simulation starts, so the agent must be leaving home
			this.goingHome = true; // Choose a new building to go to
			Building b = ContextManager.buildingContext.getRandomObject();
			this.route = new Route(this, b.getCoords(), b);
		}
*/		
		if (this.route == null) {
//			// route can only be null when the simulation starts, so the agent must be leaving home
		} else if (!this.route.atDestination()) {
			//Agent on the way
			this.route.travel();
		} else {
			//Agent reached destination:
			
		}

	} // step()

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

/*
	@Override
	public void setAgenda(AgendaFactory agenda) {
		this.myAgenda = agenda;
		LOGGER.info("Agenda set for Agent " + this.getID() + "("+this.getType()+")");
	}
	
	@Override
	public AgendaFactory getAgenda() {
		return this.myAgenda;
	}
*/
	/*
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}
*/
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
}
