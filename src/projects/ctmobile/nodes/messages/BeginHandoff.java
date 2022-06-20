package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * A BEGIN-HANDOFF message is sent by MSS_i to MSS_j when MSS_i learns that a mobile host
 * has moved from MSS_j cell to its own cell. See the hand-off procedure.
 */
public class BeginHandoff extends Message {

	public int h_k;
	public int mss_j;
	
	public BeginHandoff(int h_k, int mss_j) {
		this.h_k = h_k;
		this.mss_j = mss_j;
	}
	
	@Override
	public Message clone() {
		return new BeginHandoff(h_k, mss_j);
	}

}
