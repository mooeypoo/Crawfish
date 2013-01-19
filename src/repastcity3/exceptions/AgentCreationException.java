/**
 * Crawfish Prototype 
 * Agent-based Epidemic Simulation, using SEIR(S) Model
 * 
 * @version 1.0 Alpha
 * @author 	New York Institute of Technology, 2013
 * 			Dr. Cui's Research Team
 * 
 **/

package repastcity3.exceptions;

public class AgentCreationException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public AgentCreationException(String message) {
		super(message);
	}
	
	public AgentCreationException(Throwable cause) {
		super(cause);
	}

}
