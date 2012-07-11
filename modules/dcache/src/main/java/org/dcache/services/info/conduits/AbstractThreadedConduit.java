package org.dcache.services.info.conduits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple, abstract implementation of a blocking InfoConduit.  A new Thread
 * is created on enable(), this calls the abstract method activity() which is
 * presumed to block (e.g., for incoming network connections).
 * <p>
 * A further abstract method triggerActivityToReturn() allows the thread to
 * escape from activity(), so the thread can be shut down cleanly.
 *
 * @author Paul Millar
 */
abstract class AbstractThreadedConduit implements Runnable, Conduit {

	private static Logger _log = LoggerFactory.getLogger(AbstractThreadedConduit.class);

	private Thread _thd = null;
	protected int _callCount = 0;
	volatile boolean _should_run=true;


	/**
	 *  Start conduit activity.
	 */
	public void enable() {
		_callCount = 0;
		_should_run = true;

		if( _thd == null) {
			_thd = new Thread( this, this.getClass().getSimpleName() + " conduit");
			_thd.start();
			if( _log.isInfoEnabled())
				_log.info("Thread \"" + _thd.getName() + "\" started");
		} else {
			_log.error( "Request to start when thread is already running.");
		}
	}

	/**
	 *  Stop all conduit activity.
	 */
	public void disable() {
		if( _thd == null)
			return;

		_should_run = false;

		if( _log.isDebugEnabled())
			_log.debug("Signalling thread \"" + _thd.getName() + "\" to stop");

		triggerBlockingActivityToReturn();

		_log.debug("Waiting for thread to finish...");

		try {
			_thd.join();
		} catch( InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		_thd = null;
	}


	public boolean isEnabled() {
		return _thd != null;
	}


	/**
	 *  Typically, activity() will include some element that blocks.
	 *  This method should break that blocking call and cause the activity()
	 *  method to return quickly.
	 */
	abstract void triggerBlockingActivityToReturn();


	/**
	 *  A method provides some activity; typically, this method
	 *  will block, pending network activity.
	 */
	abstract void blockingActivity();



	/**
	 *  This class's private thread.  Simply loop over the
	 *  (subclass-specific) blocking activity.
	 */
	public void run() {
		while( _should_run)
			blockingActivity();

		if( _log.isInfoEnabled())
			_log.info("Thread " + _thd.getName() + " stopped");
	}


	/**
	 * Since we anticipate each conduit to have only one instance, we return the Class simple
	 * name here.
	 */
	@Override
    public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Return some metadata about this conduit.
	 */
	public String getInfo() {
		StringBuilder sb = new StringBuilder();

		sb.append( "[");
		sb.append( isEnabled() ? "enabled" : "disabled");

		if( isEnabled()) {
			sb.append( ", ");
			sb.append( _callCount);
		}
		sb.append( "]");

		return sb.toString();
	}
}