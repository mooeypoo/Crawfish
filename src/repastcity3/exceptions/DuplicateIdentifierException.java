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

/**
 * Exception is thrown when an object tries to set a unique identifier that has already
 * been used.
 * @author Nick Malleson
 */
public class DuplicateIdentifierException extends Exception {

	private static final long serialVersionUID = 1L;

	public DuplicateIdentifierException(String message) {
		super(message);
	}

}
