package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Positive acknowledgment sent to the coordinator. If the coordinator gathers a majority
 * of positive acknowledgments, the set V_c is locked and broadcasted as the decided set
 * of values to all base stations. Otherwise the coordinator moves to phase 1 and initiates
 * the next round. See actions 12, 8 and 14.
 */
public class PA extends Message {

	public int mss_j;
	public int r_j;
	
	public PA(int mss_j, int r_j) {
		this.mss_j = mss_j;
		this.r_j = r_j;
	}
	
	@Override
	public Message clone() {
		return new PA(mss_j, r_j);
	}

}
