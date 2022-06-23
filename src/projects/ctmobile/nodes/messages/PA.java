package projects.ctmobile.nodes.messages;

import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
import sinalgo.nodes.messages.Message;

/**
 * Positive acknowledgment sent to the coordinator. If the coordinator gathers a majority
 * of positive acknowledgments, the set V_c is locked and broadcasted as the decided set
 * of values to all base stations. Otherwise the coordinator moves to phase 1 and initiates
 * the next round. See actions 12, 8 and 14.
 */
public class PA extends Message {

	public MobileSupportStation mss;
	public int r;
	
	public PA(MobileSupportStation mss, int r) {
		this.mss = mss;
		this.r = r;
	}
	
	@Override
	public Message clone() {
		return new PA(mss, r);
	}

	@Override
	public String toString() {
		return "PA(" + mss + ", " + r + ")";
	}
}
