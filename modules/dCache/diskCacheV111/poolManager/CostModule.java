// $Id: CostModule.java,v 1.2 2006-10-10 13:50:50 tigran Exp $

package diskCacheV111.poolManager ;

import diskCacheV111.pools.PoolCostInfo;
import diskCacheV111.vehicles.PoolCostCheckable;
import dmg.cells.nucleus.CellMessage;


/**
 *  A Class that implements CostModule assigned a cost to each known pool.  This cost
 *  is in the form of a PoolCostInfo object
 */
public interface CostModule {

   /**
    * Used by PoolManager to send the replies PoolManager sends to doors.
    */
   public void messageArrived( CellMessage cellMessage ) ;

   public boolean isActive() ;

   /**
    * Obtain the cost information for transferring a file of the given size
    * for the named pool.
    * <p>
    * If the pool is not known or the CostModule value is not valid and updates
    * are switched off then null is returned.
    * @param poolName the name of the pool
    * @param filesize the length of the file to be transferred.
    * @return a PoolCostCheckable object that describes the costs of
    * transferring a file of the given size involving the named pool or null
    * if the pool is unknown or updates are switched off.
    */
   public PoolCostCheckable getPoolCost( String poolName , long filesize ) ;

   /**
    * Obtain the PoolCostInfo associated with the named pool.  If the pool is unknown
    * then null is returned.
    * @param poolName the name of a pool
    * @return PostCostInfo corresponding to the named pool.
    */
   public PoolCostInfo getPoolCostInfo(String poolName);

   /**
    * Obtain the n-percentile performance cost, that is the cost of the nth pool when
    * they have been sorted in increasing performance cost and
    * n = floor( fraction * numberOfPools).
    * @throws IllegalArgumentException if fraction <= 0 or >= 1
    * @return the n-th percentile performance cost, or 0 if no pools are known.
    */
   public double getPoolsPercentilePerformanceCost( double fraction);
}