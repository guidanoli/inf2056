package projects.ctmobile.nodes.messages;

import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
import sinalgo.nodes.messages.Message;

/**
 * Negative acknowledgment sent to the coordinator. See actions 12, 13, 9 and 14
 */
public class NA extends Message {

	public MobileSupportStation mss;
	public int r;
	
	public NA(MobileSupportStation mss, int r) {
		this.mss = mss;
		this.r = r;
	}
	
	@Override
	public Message clone() {
		return new NA(mss, r);
	}

	@Override
	public String toString() {
		return "NA(" + mss + ", " + r + ")";
	}
}
