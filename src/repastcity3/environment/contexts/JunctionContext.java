/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 **/

package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.environment.Junction;
import repastcity3.main.GlobalVars;

public class JunctionContext extends DefaultContext<Junction> {
	
	public JunctionContext() {
		super(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);
	}

}
