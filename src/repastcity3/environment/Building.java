/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 **/

package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.agent.IAgent;
import repastcity3.exceptions.NoIdentifierException;

public class Building implements FixedGeography, Identified {

	private static Logger LOGGER = Logger.getLogger(Building.class.getName());
	
	/** The type of this building. 1 means a normal house, 2 means a bank.*/
	private int type = 1;
	
	private int buildingID = 0;
	/** Number of times this house has been burgled */
	private int numBurglaries = 0;
	
	private int numInfectious = 0;
	private int numTotalInHouse = 0;
	/** A list of agents who live here */
	private List<IAgent> agents;

	/**
	 * A unique identifier for buildings, usually set from the 'identifier' column in a shapefile
	 */
	private String identifier;

	/**
	 * The coordinates of the Building. This is also stored by the projection that contains this Building but it is
	 * useful to have it here too. As they will never change (buildings don't move) we don't need to worry about keeping
	 * them in sync with the projection.
	 */
	private Coordinate coords;

	public Building() {
		this.agents = new ArrayList<IAgent>();
		this.buildingID++;
	}

	@Override
	public Coordinate getCoords() {
		return this.coords;
	}

	@Override
	public void setCoords(Coordinate c) {
		this.coords = c;

	}

	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This building has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}

	public void setIdentifier(String id) {
		this.identifier = id;
	}

	
	
	
	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
	}

	@Override
	public String toString() {
		return "building: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Building))
			return false;
		Building b = (Building) obj;
		return this.identifier.equals(b.identifier);
	}

	/**
	 * Returns the hash code of this <code>Building</code>'s identifier string. 
	 */
	@Override
	public int hashCode() {
		if (this.identifier==null) {
			LOGGER.severe("hashCode called but this object's identifier has not been set. It is likely that you're " +
					"reading a shapefile that doesn't have a string column called 'identifier'");
		}

		return this.identifier.hashCode();
	}
	
	/**
	 * Find the type of this building, represented as an integer. 1 means a normal house, 2 means a bank.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Set the type of this building, represented as an integer. 1 means a normal house, 2 means a bank.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Find the number of times that this house has been burgled.
	 */
	public int getNumBurglaries() {
		return this.numBurglaries;
	}
	
	/**
	 * Tell the house it has been burgled (increase it's burglary counter).
	 */
	public synchronized void burgled() {
		this.numBurglaries++;
	}

	/*** INFECTIOUSNESS **/
	public synchronized void agentIn(boolean isInfectious) {
		if (isInfectious == true) {
			this.numInfectious++;
		}
		this.numTotalInHouse++;
	}
	
	public synchronized void agentOut(boolean isInfectious) {
		if (isInfectious == true) {
			this.numInfectious--;
		}
		this.numTotalInHouse--;
	}
	
	public int getInfected() {
		return this.numInfectious;
	}
	
	public int getAgentsInHouse() {
		return this.numTotalInHouse;
	}
	/*** END INFECTIOUSNESS **/
	
	
/*
 * 	public void setID(int id) {

		this.buildingID = id;
	}
*/
	public int getID() {
		return this.buildingID;
	}
	
}
