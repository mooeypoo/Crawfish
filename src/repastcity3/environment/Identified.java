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

import repastcity3.exceptions.DuplicateIdentifierException;
import repastcity3.exceptions.NoIdentifierException;

/**
 * Interface for classes that can be identified. Useful for environment objects that must read an identifier
 * value from input data.
 * @author Nick Malleson
 *
 */
public interface Identified {
	
	String getIdentifier() throws NoIdentifierException;

	void setIdentifier(String id) throws DuplicateIdentifierException;

}
