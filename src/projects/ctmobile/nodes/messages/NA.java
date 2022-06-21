package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Negative acknowledgment sent to the coordinator. See actions 12, 13, 9 and 14
 */
public class NA extends Message {

	public int mss_j;
	public int r_j;
	
	public NA(int mss_j, int r_j) {
		this.mss_j = mss_j;
		this.r_j = r_j;
	}
	
	@Override
	public Message clone() {
		return new NA(mss_j, r_j);
	}

	@Override
	public String toString() {
		return "NA(" + mss_j + ", " + r_j + ")";
	}
}
