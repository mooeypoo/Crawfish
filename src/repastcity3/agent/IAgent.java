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

import java.util.List;

import repastcity3.environment.Building;

/**
 * All agents must implement this interface so that it the simulation knows how
 * to step them.
 * 
 * @author Nick Malleson
 * 
 */
public interface IAgent {
	

	/**
	 * Controls the agent. This method will be called by the scheduler once per
	 * iteration.
	 */
	 void step() throws Exception;

	/**
	 * Used by Agents as a means of stating whether or not they can be
	 * run in parallel (i.e. there is no inter-agent communication which will
	 * make parallelisation non-trivial). If all the agents in a simulation
	 * return true, and the computer running the simulation has
	 * more than one core, it is possible to step agents simultaneously.
	 * 
	 * @author Nick Malleson
	 */
	boolean isThreadable();

	/**
	 * Set health status
	 */
	
	
	public static enum DiseaseStages { 
		S(0,false,false), E(10, false,true), I(20, true,true), R(30, false,false); 
		
		private int code;
		private Boolean symptomatic;
		private Boolean infectious;
		
		private DiseaseStages(int c, Boolean s, Boolean i) {
			code = c;
			symptomatic = s;
			infectious = i;
		}
		
		public int getCode() {
			return code;
		}
		public Boolean isInfectious() {
			return infectious;
		}
		public Boolean isSymptomatic() {
			return symptomatic;
		}
	}
	
	public void setMyHealth(DiseaseStages st);
	public DiseaseStages getMyHealth();

	public void setStayHome(Boolean yesno);
	public Boolean getStayHome();
	
	
	/**
	 * Set where the agent lives.
	 */
	void setHome(Building home);
	
	/**
	 * Get the agent's home.
	 */
	Building getHome();
	
	/**
	 * Set the agent type
	 * @param type Agent type; 0: Adult; 1: Child; 2: Teenager
	 */
	void setType(int type);
	
	/**
	 * Get the agent type
	 */
	int getType();
	
	/**
	 * Set parents
	 */
	void setMother(IAgent person);
	void setFather(IAgent person);
	void setSibling(IAgent person);
	void setPartner(IAgent person);
	
	/**
	 * Get parents
	 */
	IAgent getMother();
	IAgent getFather();
	IAgent getSibling();
	IAgent getPartner();
	
	/**
	 * 
	 */
	
	/**
	 * (Optional). Add objects to the agents memory. Used to keep a record of all the
	 * buildings that they have passed.
	 * @param <T>
	 * @param objects The objects to add to the memory.
	 * @param clazz The type of object.
	 */
	<T> void addToMemory(List<T> objects, Class<T> clazz);
	
	/**
	 * (Optional). Get the transport options available to this agent. E.g.
	 * an agent with a car who also could use public transport would return
	 * <code>{"bus", "car"}</code>. If null then it is assumed that the agent
	 * walks (the slowest of all transport methods). 
	 */
	List<String> getTransportAvailable();
	
	
}
