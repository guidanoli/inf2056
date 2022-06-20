package projects.ctmobile.nodes.messages;

import java.util.HashSet;

import sinalgo.nodes.messages.Message;

/**
 * This message carries the decided value. A base station MSS_i receives a message
 * DECIDE(V_j) when a coordinator is aware that a majority of base stations agree
 * upon the set of values V_j. MSS_i adopts this value, changes its state to decided,
 * forwards the decided set of values to local mobile hosts and terminates. To ensure
 * that all correct processes decide, the message is also forwarded to the other base
 * stations (reliable broadcast). See the hand-off procedure and actions 14, 6 and 3.
 */
public class Decide<T> extends Message {

	public HashSet<T> V_j;
	
	@SuppressWarnings("unchecked")
	public Decide(HashSet<T> V_j) {
		this.V_j = (HashSet<T>)V_j.clone();
	}
	
	@Override
	public Message clone() {
		return new Decide<T>(V_j);
	}

}
