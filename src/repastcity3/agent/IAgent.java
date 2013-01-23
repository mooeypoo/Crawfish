/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 * IAgent
 * Defines the interface of a single agent
 * 
 **/


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

	public void createWorkplace();
	public Building getWorkplace();
	
	/**
	 * Set health status
	 */
	public static enum DiseaseStages { 
		S(0,false,false), E(10, false,true), I(20, true,true), R(30, false,false), D(999, false, false); 
		
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
	
	public void setHealthStatus(DiseaseStages st);
	public DiseaseStages getHealthStatus();

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
	int getID();
	void setID(int id);
	/**
	 * Set parents
	 */
	void setMother(int person);
	void setFather(int person);
	void setSibling(int person);
	void setPartner(int person);
	void setChild1(int person);
	void setChild2(int person);
	
	/**
	 * Get parents
	 */
	int getMother();
	int getFather();
	int getSibling();
	int getPartner();
	int getChild1();
	int getChild2();
	
	boolean isHasChildren();
	void setHasChildren(boolean hasChildren);
	public void setChildren(int c1, int c2);
	
	/**
	 * 
	 */
/*
	void setAgenda(AgendaFactory agenda);
	AgendaFactory getAgenda();
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
