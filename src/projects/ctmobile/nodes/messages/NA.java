package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Negative acknowledgment sent to the coordinator. See actions 12, 13, 9 and 14
 */
public class NA extends Message {

	public int MSS_j;
	public int r_j;
	
	public NA(int MSS_j, int r_j) {
		this.MSS_j = MSS_j;
		this.r_j = r_j;
	}
	
	@Override
	public Message clone() {
		return new NA(MSS_j, r_j);
	}

}
