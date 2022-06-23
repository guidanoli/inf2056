package projects.ctmobile.nodes.messages;

import java.util.HashSet;

import projects.ctmobile.nodes.nodeImplementations.MobileHost;
import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
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
public class NewEstimate extends Message {

	public MobileSupportStation mss;
	public int r;
	public HashSet<Integer> v;
	public HashSet<MobileHost> p;
	public boolean endCollect;

	public NewEstimate(MobileSupportStation mss, int r, HashSet<Integer> v, HashSet<MobileHost> p, boolean endCollect_c) {
		this.mss = mss;
		this.r = r;
		this.v = new HashSet<Integer>(v);
		this.p = new HashSet<MobileHost>(p);
		this.endCollect = endCollect_c;
	}
	
	@Override
	public Message clone() {
		return new NewEstimate(mss, r, v, p, endCollect);
	}

	@Override
	public String toString() {
		return "NEW_ESTIMATE(" +
				mss + ", " +
				r + ", " +
				v + ", " +
				p + ", " +
				endCollect + ")";
	}
}
