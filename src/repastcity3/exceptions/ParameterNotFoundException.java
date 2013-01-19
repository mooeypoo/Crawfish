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

public class ParameterNotFoundException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ParameterNotFoundException(String param) {
		super("Could not find the Simphony parameter "+param+
				". Has it been specified in parameters.xml ?");
	}

}
