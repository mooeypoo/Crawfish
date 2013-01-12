package repastcity3.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import java.util.logging.Logger;


/**
 * Create and handle agendas
 * @author moriel
 *
 */
public class AgendaFactory {
	private static Logger LOGGER = Logger.getLogger(AgendaFactory.class.getName());
	
	private enum agentType {
		Adult(0), Child(5), Teenager(10);
		private int agType;
		private agentType(int c) {
			agType = c;
		}
		
		public int getType() {
			return agType;
		}
	}
	
	private agentType myType;

	private enum activity {
		Home(0), House(1), Work(2), School(3), Kindergarden(4), Mall(5), Lunch(5);

		private int shpfileCode;
		private activity(int c) {
			shpfileCode = c;
		}
		
		public int getCode() {
			return shpfileCode;
		}
	}

	HashMap<Double, activity> fullAgenda = new HashMap<Double, activity>();
	
	
	
	/**
	 * CONSTRUCTOR, Create the agendas
	 * @param type Agent Type (Adult, Child, Teenager)
	 */
	public AgendaFactory(agentType type) {
		
		Random randomGenerator = new Random(123987);
		int rand = randomGenerator.nextInt(100);
		
		switch (type) {
		case Adult:
			fullAgenda.put(new Double(8.0), activity.Work);
			fullAgenda.put(new Double(12.0), activity.Lunch);
			fullAgenda.put(new Double(13.0), activity.Work);
			//pick random:
			if (rand > 50) {
				fullAgenda.put(new Double(17.0), activity.Mall);
				fullAgenda.put(new Double(19.0), activity.Home);
			} else {
				fullAgenda.put(new Double(17.0), activity.Home);
			}
			/** ADD ACTIVITY IF ADULT IS WITHOUT KIDS **/
			break;
		case Child:
			fullAgenda.put(new Double(8.0), activity.Kindergarden);
			fullAgenda.put(new Double(15.0), activity.Kindergarden); //"Child care"
			fullAgenda.put(new Double(18.0), activity.Home);
			break;
			
		case Teenager:
			fullAgenda.put(new Double(8.0), activity.School);
			fullAgenda.put(new Double(15.0), activity.Home);
			if (rand > 50) {
				fullAgenda.put(new Double(16.0), activity.Mall);
			} else {
				fullAgenda.put(new Double(16.0), activity.House);
			}
			fullAgenda.put(new Double(19.0), activity.Home);
			break;
		}
		
	}

	
	public activity getItem(double time) {
		return fullAgenda.get(time);
	}
	
	
	
	public void setType(agentType type) {
		this.myType = type;
	}
	
	public agentType getType() {
		return this.myType;
	}
	
}
