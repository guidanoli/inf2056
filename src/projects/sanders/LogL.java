package projects.sanders;

/**
 * Enumerates the log-levels. Levels above THRESHOLD will be included
 * in the log-file. The levels below (with a higher enumeration value) not.
 */
public class LogL extends sinalgo.tools.logging.LogL{
	/**
	 * when nodes change state (in the state machine)
	 */
	public static final boolean NODE_STATE_CHANGE = false;
	/**
	 * when a node relinquishes a vote
	 */
	public static final boolean RELINQUISH = true;
}


