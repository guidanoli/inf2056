package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Such a message is sent by a mobile host to inform the current base station
 * when it enters a new cell. See the hand-off procedure.
 */
public class Guest extends Message {

	public int h_k;
	public int MSS_j;
	
	public Guest(int h_k, int MSS_j) {
		this.h_k = h_k;
		this.MSS_j = MSS_j;
	}
	
	@Override
	public Message clone() {
		return new Guest(h_k, MSS_j);
	}

}
