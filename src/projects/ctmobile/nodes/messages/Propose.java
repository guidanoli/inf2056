package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

// Such a message carries the value proposed by a mobile host
// to its local base station [3]. A base station MSSi takes it
// into account if |Pi| < alpha.
//
// [3] The reader can notice that the value proposed by a mobile host
// is not required to be always the same. This possibility is not
// discussed in this paper.
public class Propose extends Message {

	public int h_k;
	public int v_k;
	
	/**
	 * @param h_k mobile host id
	 * @param v_k proposed value
	 */
	public Propose(int h_k, int v_k) {
		this.h_k = h_k;
		this.v_k = v_k;
	}
	
	@Override
	public Message clone() {
		return new Propose(h_k, v_k);
	}

}
