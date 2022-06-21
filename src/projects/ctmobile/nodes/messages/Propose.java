package projects.ctmobile.nodes.messages;

import projects.ctmobile.nodes.nodeImplementations.MobileHost;
import sinalgo.nodes.messages.Message;

// Such a message carries the value proposed by a mobile host
// to its local base station [3]. A base station MSSi takes it
// into account if |Pi| < alpha.
//
// [3] The reader can notice that the value proposed by a mobile host
// is not required to be always the same. This possibility is not
// discussed in this paper.
public class Propose extends Message {

	public MobileHost mh;
	public int v;
	
	/**
	 * @param mh mobile host
	 * @param v_k proposed value
	 */
	public Propose(MobileHost mh, int v_k) {
		this.mh = mh;
		this.v = v_k;
	}
	
	@Override
	public Message clone() {
		return new Propose(mh, v);
	}

	@Override
	public String toString() {
		return "PROPOSE(" + mh + ", " + v + ")";
	}
}
