package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * A BEGIN-HANDOFF message is sent by MSS_i to MSS_j when MSS_i learns that a mobile host
 * has moved from MSSj cell to its own cell. See the hand-off procedure.
 */
public class BeginHandoff extends Message {

	public int h_k;
	public int MSS_j;
	
	public BeginHandoff(int h_k, int MSS_j) {
		this.h_k = h_k;
		this.MSS_j = MSS_j;
	}
	
	@Override
	public Message clone() {
		return new BeginHandoff(h_k, MSS_j);
	}

}
