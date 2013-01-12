package repastcity3.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import java.util.logging.Logger;

import repastcity3.main.GlobalVars;


/**
 * Create and handle agendas
 * @author moriel&sanem
 *
 */
public class AgendaFactory {
	private static Logger LOGGER = Logger.getLogger(AgendaFactory.class.getName());

	private static final int ADULT = 0;
	private static final int CHILD = 5;
	private static final int TEEN = 10;

	private int myType;

/*
	private enum activity {
		Home(0), 
		House(1), 
		Work(2), 
		School(3), 
		Kindergarden(4), 
		Mall(5), 
		Lunch(6);

		private int value;
		private activity(int c) {
			this.value = c;
		}
		
		public int numeric() {
			return this.value;
		}
	}
*/
	
	private HashMap<String, Integer> fullAgendaAdult = new HashMap<String, Integer>();
	private HashMap<String, Integer> fullAgendaTeen = new HashMap<String, Integer>();
	private HashMap<String, Integer> fullAgendaChild = new HashMap<String, Integer>();
	
	
	/**
	 * CONSTRUCTOR, Create the agendas
	 * @param agentType Agent Type (Adult, Child, Teenager)
	 */
	public AgendaFactory() {
		makeAdultAgenda();
		makeTeenAgenda();
		makeChildAgenda();
		
	}

	public void makeTeenAgenda() {
		fullAgendaTeen.put("8.0", GlobalVars.ACT_SCHOOL);
		fullAgendaTeen.put("15.0", GlobalVars.ACT_HOME);
		Random randomGenerator = new Random(123987);
		int rand = randomGenerator.nextInt(100);
		if (rand > 50) {
			fullAgendaTeen.put("16.0", GlobalVars.ACT_MALL);
		} else {
			fullAgendaTeen.put("16.0", GlobalVars.ACT_HOUSE);
		}
		fullAgendaTeen.put("19.0", GlobalVars.ACT_HOME);
	}
	
	public void makeChildAgenda() {
		fullAgendaChild.put("8.0", GlobalVars.ACT_KINDERGARTEN);
		fullAgendaChild.put("15.0", GlobalVars.ACT_KINDERGARTEN);
		fullAgendaChild.put("18.0", GlobalVars.ACT_HOME);
	}
	
	public void makeAdultAgenda() {
		Random randomGenerator = new Random(123987);
		int rand = randomGenerator.nextInt(100);
		fullAgendaAdult.put("8.0", GlobalVars.ACT_WORK);
		fullAgendaAdult.put("12.0", GlobalVars.ACT_LUNCH);
		fullAgendaAdult.put("13.0", GlobalVars.ACT_WORK);
		//pick random:
		if (rand > 50) {
			fullAgendaAdult.put("17.0", GlobalVars.ACT_MALL);
			fullAgendaAdult.put("19.0", GlobalVars.ACT_HOME);
		} else {
			fullAgendaAdult.put("17.0", GlobalVars.ACT_HOME);
		}
	}

	
	
	
	public int getItem(int agentType, double time) {
		String timeString = String.valueOf(time);
		int ans = 0;
		switch (agentType) {
		case ADULT:
			ans = fullAgendaAdult.get(timeString);
		case TEEN:
			ans = fullAgendaTeen.get(timeString);
		case CHILD:
			ans = fullAgendaChild.get(timeString);
		}
		return ans;
	}
	
	
	
	public void setType(int type) {
		this.myType = type;
	}
	
	public int getType() {
		return this.myType;
	}
	
}
