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
	
	private int diseaseStatNumber = -1;
	
	private double timeSpentInLocation = 0;

	private Boolean isImmune = false;
	/**
	 * Boolean stayHome
	 * false if going to work/school/kindergarden
	 * true if sick child, or if a parent of a sick child 
	 */
	private Boolean stayHome = false; //set true if parent of a sick child
	
	private List<Building> nearbyBuildings = new ArrayList<Building>();
	private List<Building> buildingsVisitedToday = new ArrayList<Building>();

	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Building mall; // random mall for recreation
	private Route route; // An object to move the agent around the world

	private Building previousBuilding; //whatever we visited last
	private Building currentBuilding; //the building the agent is currently located
	private boolean alreadyUpdatedBuilding = false;
	private boolean goingHome = false; // Whether the agent is going to or from their home
	private boolean isHome = true;
	
	private int agentType = 0;
	
	private DiseaseStages myHealthStatus = DiseaseStages.S;
	private static int counter =0;
	private static int uniqueID = 0;
	private int id;

	private boolean hasChildren =false;
//	private AgendaFactory myAgenda;
	
	/*** COUNTERS ***/
	private int cHealthExposed = 0;
	private int cHealthInfected = 0;
	private int cHealthImmune = 0;
	/*** END COUNTERS ***/
	
	
	public DefaultAgent() {
//		this.workplace = createWorkplace();
		this.previousBuilding = this.home;
		this.currentBuilding = this.home;
		this.setHealthStatus(DiseaseStages.S);
	}

	@Override
	public void step() throws Exception {
		if (!this.getHealthStatus() == DiseaseStages.D) {
			/** CHECK TIME OF DAY **/
			double theTime = BigDecimal.valueOf(ContextManager.realTime).
			        round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();
	
			//skip everything if the user is going home:
			if (this.getGoHome()==false) {
				//to save calculation time, only check agenda items on 60-min intervals:
				if ((theTime%0.5) == 0) {
					Building nextDestBuilding = this.getNextAgendaItem(theTime);
					if ((nextDestBuilding != null)) { 
						// Leaving towards new destination.
						this.alreadyUpdatedBuilding = false;
						this.route = new Route(this, nextDestBuilding.getCoords(), nextDestBuilding); // Create a route to work
					}
				}
			}
			
			if (theTime == 0) {
				System.out.println("Agent "+this.getID()+" DAILY HealthStatus Change.");
				//Check health status at home:
				/****************************************************/
				double oddsOfInfectiousness = this.calcInfectiousness(this.home);
				if (checkIfAgentIsSick(oddsOfInfectiousness) == true) {
					//sick
					System.out.println("Agent#"+this.getID()+": (At Home) EXPOSED");
				} else {
					//not sick
					System.out.println("Agent#"+this.getID()+": (At Home) NOT EXPOSED");
				}
				/****************************************************/
				
				/// Infection status counters:
				if (this.isImmune == true) {
					System.out.println("Agent"+this.getID()+" is Immune.");
					this.cHealthImmune++;
					if (this.cHealthImmune > GlobalVars.hCOUNTER_IMMUNE) {
						this.isImmune = false;
						this.cHealthImmune = 0;
						System.err.println("Agent "+this.getID()+": No longer immune.");
					}
				} else {
					System.out.println("Agent"+this.getID()+" is "+this.getHealthStatus().toString() + " --> CHECKING CHANGE.");
					if (this.getHealthStatus() == DiseaseStages.E) {
						this.cHealthExposed++;
						System.out.println("Agent"+this.getID()+" Exposed Counter: "+this.cHealthExposed);
						if (this.cHealthExposed > GlobalVars.hCOUNTER_EXPOSED) {
							//move on to next stage:
							this.setHealthStatus(DiseaseStages.I);
							this.cHealthExposed=0;
							System.err.println("Agent "+this.getID()+": Now INFECTED.");
						}
					} else if (this.getHealthStatus() == DiseaseStages.I) {
						this.cHealthInfected++;
						System.out.println("Agent"+this.getID()+" Infected Counter: "+this.cHealthInfected);
						if (this.cHealthInfected > GlobalVars.hCOUNTER_INFECTED) {
							//move on to next stage:
							Random randomGenerator = new Random(123987);
							int randDice = randomGenerator.nextInt(100);
							if (randDice <= (GlobalVars.DISEASE_PERC_DEATHS)) {
								//agent is dead :(
								System.err.println("Agent "+this.getID()+": DEAD.");
								//remove from family:
								if (this.getType()==GlobalVars.P_ADULT) {
									//check if there is a partner:
									if (this.getPartner( ) > -1) {
										//remove from partner:
										GlobalVars.popListAdult.get(this.getPartner()).setPartner(-1);
									}
									if (this.getChild1() > -1) {
										//remove from child
										if (this.getID() == GlobalVars.popListChild.get(this.getChild1()).getFather()) {
											GlobalVars.popListChild.get(this.getChild1()).setFather(-1);
										} else if (this.getID() == GlobalVars.popListChild.get(this.getChild1()).getMother()) {
											GlobalVars.popListChild.get(this.getChild1()).setMother(-1);
										}
									}
									if (this.getChild2() > -1) {
										//remove from child
										if (this.getID() == GlobalVars.popListChild.get(this.getChild2()).getFather()) {
											GlobalVars.popListChild.get(this.getChild2()).setFather(-1);
										} else if (this.getID() == GlobalVars.popListChild.get(this.getChild2()).getMother()) {
											GlobalVars.popListChild.get(this.getChild2()).setMother(-1);
										}
									}
									// set status to DEAD:
									this.setHealthStatus(DiseaseStages.D);
									/*******************************************/
									/*******************************************/
									/*******************************************/
									/** DO WE ERASE THE PERSON FROM THE LIST? **/
									/*******************************************/
									/*******************************************/
									/*******************************************/
									
								} else {
									//remove from parents:
									int momIndex = this.getMother();
									if (this.getID()==GlobalVars.popListAdult.get(momIndex).getChild1()) {
										GlobalVars.popListAdult.get(momIndex).setChild1(-1);
									} else if (this.getID()==GlobalVars.popListAdult.get(momIndex).getChild2()) {
										GlobalVars.popListAdult.get(momIndex).setChild2(-1);
									}
									int dadIndex = this.getFather();
									if (this.getID()==GlobalVars.popListAdult.get(dadIndex).getChild1()) {
										GlobalVars.popListAdult.get(dadIndex).setChild1(-1);
									} else if (this.getID()==GlobalVars.popListAdult.get(dadIndex).getChild2()) {
										GlobalVars.popListAdult.get(dadIndex).setChild2(-1);
									}
									//remove from sibling:
									if (this.getSibling() > -1) {
										GlobalVars.popListChild.get(this.getSibling()).setSibling(-1);
									}
									// set status to DEAD:
									this.setHealthStatus(DiseaseStages.D);
									/*******************************************/
									/*******************************************/
									/*******************************************/
									/** DO WE ERASE THE PERSON FROM THE LIST? **/
									/*******************************************/
									/*******************************************/
									/*******************************************/
								}
								
							} else {
								//agent has recovered!
								System.err.println("Agent "+this.getID()+": Now BACK TO SUSCEPTIBE.");
								this.setHealthStatus(DiseaseStages.S); //back to susceptible
							}
							
							this.cHealthInfected=0;
						}
					}
				}
				
				/*****************************************
				 *****************************************
				 *****************************************
				 *****************************************
				 **   RESET BUILDINGS OF THE DAY HERE   **
				 *****************************************
				 *****************************************
				 *****************************************
				 *****************************************
				 */
				if (!this.getHealthStatus().isInfectious()) { //agent healed
					for (Building b : this.buildingsVisitedToday) {
						synchronized (ContextManager.randomLock) {
							Iterator<Building> bList = ContextManager.buildingContext.getRandomObjects(Building.class, 10000).iterator();
							String msg = "";
							while (bList.hasNext()) {
								Building bld = bList.next();
								if (bld.equals(b)) {
									bld.removeSickAgent(this.getID());
								}
							}
						}
					}
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
	//				if (!visitBuilding(this.previousBuilding,false)) {//go out
	//					System.err.println("ERR: NO BUILDING FOUND (previousBuilding)");
	//				}
					if (!visitBuilding(this.currentBuilding,true)) { //go in
						System.err.println("ERR: NO BUILDING FOUND (currentBuilding)");
					}
					this.alreadyUpdatedBuilding = true;
					
				}
			} else if (!this.route.atDestination()) {
				//Agent is traveling
	
				this.alreadyUpdatedBuilding = false;
				if (this.getStayHome()==true && this.getIsHome()==true) {
					//stay home.
					System.out.println("Agent" + this.getID()+ " STAYS HOME.");
				} else {
					this.route.travel();
				}
	
			} else if (this.route.atDestination()) {
				//Agent reached destination. 
				this.previousBuilding = this.currentBuilding;
				this.currentBuilding = this.route.getDestinationBuilding();
				if (this.alreadyUpdatedBuilding==false) {
					
					// CALCULATE INFECTIOUSNESS
					if (this.route.getDestinationBuilding().equals(this.home)) {
						this.setIsHome(true);
					} else {
						this.setIsHome(false);
					}
	
					if (this.previousBuilding !=null) {
						if (this.isImmune==false) {
							if (!this.getHealthStatus().isInfectious() && this.getHealthStatus() != DiseaseStages.D) { 
			
								double oddsOfInfectiousness = this.calcInfectiousness(this.previousBuilding);
								if (checkIfAgentIsSick(oddsOfInfectiousness)) {
									//sick
									this.setHealthStatus(DiseaseStages.E);
								} else {
									//not sick
								}
								
							}
	
							this.alreadyUpdatedBuilding = true;
						}
					}
				}
	
				if (this.getGoHome()==true && this.getIsHome()==false) {
					// SEND AGENT HOME:
					this.route = new Route(this, this.home.getCoords(), this.home); // Create a route to home
					System.out.println("Agent" + this.getID()+ " IS GOING HOME.");
					
				} else {
					this.route = null;
					this.alreadyUpdatedBuilding = false;
					this.timeSpentInLocation = 0;
				}
			}
		} // if not dead
	} // step()

	public boolean checkIfAgentIsSick(double oddsOfInfectiousness) {
		boolean isSick = false;
//		double oddsOfInfectiousness = this.calcInfectiousness(this.previousBuilding);
//		System.out.println("["+theTime+"] *** Building #"+this.previousBuilding.hashCode()+ "["+this.previousBuilding.getType()+"] infectiousness: "+oddsOfInfectiousness);
		// Toss the dice:
		Random randomGenerator = new Random(123987);
		int randDice = randomGenerator.nextInt(100);
		if (randDice <= oddsOfInfectiousness) {
			this.setHealthStatus(DiseaseStages.E);
			isSick = true;
//			System.err.println("Agent #"+this.getID() + " is Infected");
		} else {
//			System.err.println("Agent #"+this.getID() + " is SAFE! (Not Infected)");
			isSick = false;
		}
		return isSick;
	}
	
	public boolean visitBuilding(Building b, boolean amIGoingIn) {
		synchronized (ContextManager.randomLock) {
			Iterator<Building> bList = ContextManager.buildingContext.getRandomObjects(Building.class, 10000).iterator();
			String msg = "";
			while (bList.hasNext()) {
				Building bld = bList.next();
				if (bld.equals(b)) {
					if (amIGoingIn==true) {
						msg = "IN";
						bld.agentIn(this.getID(), this.getHealthStatus().isInfectious());
						/** Add to buildings of the day: **/
						if (this.buildingsVisitedToday.indexOf(b) == -1) {
							//add
							this.buildingsVisitedToday.add(b);
						}
						
					} else {
						msg = "OUT";
//							bld.agentOut(this.getID(), this.getHealthStatus().isInfectious());
					}
//					System.out.println("Agent "+this.getID()+": GOING "+msg);
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
			if (b != null) {
				int totalPop =0;
				if (b.getType()==1) { //if agent is in a house
					totalPop=4;
				} else {
					if (this.getType()==GlobalVars.P_ADULT) {
						totalPop = GlobalVars.popListAdult.size() / 4;
					} else {
						totalPop = GlobalVars.popListChild.size() / 4;
					}
				}
				int sickPop = b.getInfected();
				result = infEquation(sickPop, totalPop, this.timeSpentInLocation);
//				System.out.println("B#"+b.hashCode()+" Infness: "+result);
			} else {
				System.err.println("ERROR: BUILDING IS NULL!!!");
			}
		return result;
	}

	public double infEquation(int infected, int total, double time) {
		double ans = 0;
		double dInfected = (double) infected;
		double dTotal = (double) total;
		ans = (5.0)*GlobalVars.InfectionFactor * (1.0)*(time / 1000) * (4.0)*(dInfected / (dTotal)) * 100;
		System.err.println("ANS: "+ans+"-> (5.0)*"+GlobalVars.InfectionFactor+" * (1.0)*("+time+" / 1000) * (4.0)*("+dInfected+" / "+(dTotal)+") * 100");
		return ans;
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
						nextPlaceStr = "Evening Drinks! " + nextPlace.getType();
					} else if (currTime == 23.0) { // 23:00
						nextPlace = this.home;
						nextPlaceStr = "Home ";
					}
				}
			} else if (this.getType() == GlobalVars.P_TEEN) { 
				if (currTime == 15.00) {
					nextPlace = this.home;
					nextPlaceStr = "Home " + nextPlace.getType();
				}
			}
		} else { 
			// stayhome == true
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
		
		String status = st.toString();
		this.setDiseaseStatNumber(st.getCode());
		System.err.println("AGENT" + this.getID() + " --> " + status);
		
		if (st == DiseaseStages.D) {
			//agent is dead!
			System.err.println("AGENT" + this.getID() + ": DEAD.");
			this.setStayHome(true);
			//don't go anywhere
			return;
		}
		
		if (st.isSymptomatic() == true) {
			if (this.getType() == GlobalVars.P_CHILD) {
				this.setStayHome(true);
				this.setGoHome(true);

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
			} else if (this.getType() == GlobalVars.P_TEEN) {
				this.setStayHome(true);
				this.setGoHome(true);
			} else {
				//don't do anything for adults
			}
		} else {
			if (this.getType() == GlobalVars.P_CHILD) {
				this.setStayHome(false);
				this.setGoHome(false);
				//let the parent go back to work (unless sibling is sick):
				if (this.getSibling()>-1) {
					if ((GlobalVars.popListChild.get(this.getSibling()).getType() == GlobalVars.P_CHILD) && (GlobalVars.popListChild.get(this.getSibling()).getHealthStatus().isSymptomatic()==true)) {
						//do nothing, sibling is a child who's sick
					}
				} else {
					GlobalVars.popListAdult.get(this.getMother()).setStayHome(false);
					GlobalVars.popListAdult.get(this.getFather()).setStayHome(false);
				}
			} else if (this.getType() == GlobalVars.P_TEEN) {
				this.setStayHome(false);
				this.setGoHome(false);
			} else {
				//do nothing for adults
			}
		}
		return;
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
	
	public void setGoHome(Boolean true_or_false) {
		this.goingHome = true_or_false;
	}
	
	public Boolean getGoHome() {
		return this.goingHome;
	}

	public void setIsHome(Boolean true_or_false) {
		this.isHome = true_or_false;
	}
	
	public Boolean getIsHome() {
		return this.isHome;
	}

	 @Override 
	 public void setDiseaseStatNumber(int dStatNum) {
	 	this.diseaseStatNumber = dStatNum;
	 }

	 @Override 
	 public int getDiseaseStatNumber() {
		 return this.diseaseStatNumber;
	 }
	
}
