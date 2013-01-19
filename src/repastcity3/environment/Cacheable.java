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

/**
 * Used by any class which has static cached objects. Static caches must be cleared at the start of
 * each simulation or they will persist over multiple simulation runs unless Simphony is restarted. 
 * @author Nick Malleson
 *
 */
public interface Cacheable {
	
	void clearCaches();

}
