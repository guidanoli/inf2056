package projects.ctmobile.nodes.messages;

import java.util.HashSet;

import sinalgo.nodes.messages.Message;

/**
 * This message carries the estimate proposed by the coordinator to the base stations.
 * When the coordinator of round r has gathered a majority of estimates, it selects one
 * estimate from its local buffer Log_c[r] and sends it as a new estimate to all base stations.
 * The selected estimate is either the new estimate sent by a previous coordinator which
 * failed to gather a majority of positive acknowledgments or the set of values New_V_c of the
 * current coordinator. While a base station MSS_i is waiting for a new estimate V_c, it asks
 * its failure detector module whether the current coordinator has crashed or not. If the
 * NEW_EST message is received before the coordinator is suspected and if it carries at least
 * a participant mobile hosts, the base station updates its set of values V_i to V_c and
 * replies with a positive acknowledgment. Otherwise it replies with a negative acknowledgment
 * and next updates its sets New_V_i and Pi. See actions 11 and 12.
 */
public class NewEstimate<T> extends Message {

	public int MSS_c;
	public int r_i;
	public HashSet<T> V_c;
	public HashSet<Integer> P_c;
	public boolean endCollect_c;
	
	@SuppressWarnings("unchecked")
	public NewEstimate(int MSS_c, int r_i, HashSet<T> V_c, HashSet<Integer> P_c, boolean endCollect_c) {
		this.MSS_c = MSS_c;
		this.r_i = r_i;
		this.V_c = (HashSet<T>)V_c.clone();
		this.P_c = (HashSet<Integer>)P_c.clone();
		this.endCollect_c = endCollect_c;
	}
	
	@Override
	public Message clone() {
		return new NewEstimate<T>(MSS_c, r_i, V_c, P_c, endCollect_c);
	}

}
