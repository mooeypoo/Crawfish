/*
ęCopyright 2012 Nick Malleson
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

package repastcity3.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repastcity3.agent.IAgent;
import repastcity3.environment.Route;
import repastcity3.agent.AgendaFactory;
/**
 * 
 * @author nick
 *
 */
public abstract class GlobalVars {
	
	private static Logger LOGGER = Logger.getLogger(GlobalVars.class.getName());
	
	/* These are strings that match entries in the repastcity.properties file.*/
	public static final String GISDataDirectory = "GISDataDirectory";
	public static final String BuildingShapefile = "BuildingShapefile";
	public static final String RoadShapefile = "RoadShapefile";
	public static final String BuildingsRoadsCoordsCache = "BuildingsRoadsCoordsCache";
	public static final String BuildingsRoadsCache = "BuildingsRoadsCache";

	/* Population Lists */
	public static final ArrayList<IAgent> popListAdult = new ArrayList<IAgent>();
	public static final ArrayList<IAgent> popListTeen = new ArrayList<IAgent>();
	public static final ArrayList<IAgent> popListChild = new ArrayList<IAgent>();
	
	public static final int ACT_HOME = 0;
	/** These are shapefile building definitions: **/
	public static final int ACT_HOUSE = 1;
	public static final int ACT_WORK = 2;
	public static final int ACT_SCHOOL = 3;
	public static final int ACT_KINDERGARTEN = 4;
	public static final int ACT_MALL = 5;
	public static final int ACT_LUNCH = 6;

	/** AGENT TYPES **/
	public static final int P_ADULT = 0;
	public static final int P_CHILD = 5;
	public static final int P_TEEN = 10;
	
	
	public static int INITIAL_INFECTED;
	
	public static int currentDay = 0;

	public static AgendaFactory Agenda = new AgendaFactory();
	
	public static final class GEOGRAPHY_PARAMS {
		
		/**
		 * Different search distances used in functions that need to find objects that are
		 * close to them. A bigger buffer means that more objects will be analysed (less
		 * efficient) but if the buffer is too small then no objects might be found. 
		 * The units represent a lat/long distance so I'm not entirely sure what they are,
		 * but the <code>Route.distanceToMeters()</code> method can be used to roughly 
		 * convert between these units and meters.
		 * @see Geometry
		 * @see Route
		 */
		public enum BUFFER_DISTANCE {
			/** The smallest distance, rarely used. Approximately 0.001m*/
			SMALL(0.00000001, "0.001"),
			/** Most commonly used distance, OK for looking for nearby houses or roads.
			 * Approximatey 50m */
			MEDIUM(0.0005,"50"),
			/** Largest buffer, approximately 550m. I use this when doing things that
			 * don't need to be done often, like populating caches.*/
			LARGE(0.005,"550");
			/**
			 * @param dist The distance to be passed to the search function (in lat/long?)
			 * @param distInMeters An approximate equivalent distance in meters.
			 */
			BUFFER_DISTANCE(double dist, String distInMeters) {
				this.dist = dist;
				this.distInMeters = distInMeters;
			}
			public double dist;
			public String distInMeters;
		}

		/** The distance that agents can travel each turn. */
		public static final double TRAVEL_PER_TURN = 50; // Slower than average (about 2mph) but good for this simulation.
	}
	
	/** Names of contexts and projections. These names must match those in the
	 * parameters.xml file so that they can be displayed properly in the GUI. */
	public static final class CONTEXT_NAMES {
		
		public static final String MAIN_CONTEXT = "maincontext";
		public static final String MAIN_GEOGRAPHY = "MainGeography";
		
		public static final String BUILDING_CONTEXT = "BuildingContext";
		public static final String BUILDING_GEOGRAPHY = "BuildingGeography";
		
		public static final String ROAD_CONTEXT = "RoadContext";
		public static final String ROAD_GEOGRAPHY = "RoadGeography";
		
		public static final String JUNCTION_CONTEXT = "JunctionContext";
		public static final String JUNCTION_GEOGRAPHY = "JunctionGeography";
		
		public static final String ROAD_NETWORK = "RoadNetwork";
		
		public static final String AGENT_CONTEXT = "AgentContext";
		public static final String AGENT_GEOGRAPHY = "AgentGeography";
	
	}
	
	// Parameters used by transport networks
	public static final class TRANSPORT_PARAMS {

		// This variable is used by NetworkEdge.getWeight() function so that it knows what travel options
		// are available to the agent (e.g. has a car). Can't be passed as a parameter because NetworkEdge.getWeight()
		// must override function in RepastEdge because this is the one called by ShortestPath.
		public static IAgent currentAgent = null;
		public static Object currentBurglarLock = new Object();

		public static final String WALK = "walk";
		public static final String BUS = "bus";
		public static final String TRAIN = "train";
		public static final String CAR = "car";
		// List of all transport methods in order of quickest first
		public static final List<String> ALL_PARAMS = Arrays.asList(new String[]{TRAIN, CAR, BUS, WALK});

		// Used in 'access' field by Roads to indicate that they are a 'majorRoad' (i.e. motorway or a-road).
		public static final String MAJOR_ROAD = "majorRoad";		
		// Speed advantage for car drivers if the road is a major road'
		public static final double MAJOR_ROAD_ADVANTAGE = 3;

		// The speed associated with different types of road (a multiplier, i.e. x times faster than walking)
		public static double getSpeed(String type) {
			if (type.equals(WALK))
				return 1;
			else if (type.equals(BUS))
				return 2;
			else if (type.equals(TRAIN))
				return 10;
			else if (type.equals(CAR))
				return 5;
			else {
				LOGGER.log(Level.SEVERE, "Error getting speed: unrecognised type: "+type);
				return 1;
			}
		}
	}
	

}
