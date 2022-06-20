package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Such a message is sent by a mobile host to inform the current base station
 * when it enters a new cell. See the hand-off procedure.
 */
public class Guest extends Message {

	public int h_k;
	public int mss_j;
	
	public Guest(int h_k, int mss_j) {
		this.h_k = h_k;
		this.mss_j = mss_j;
	}
	
	@Override
	public Message clone() {
		return new Guest(h_k, mss_j);
	}

}
