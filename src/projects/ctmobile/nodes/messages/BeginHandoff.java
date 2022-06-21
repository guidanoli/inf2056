package projects.ctmobile.nodes.messages;

import projects.ctmobile.nodes.nodeImplementations.MobileHost;
import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
import sinalgo.nodes.messages.Message;

/**
 * A BEGIN-HANDOFF message is sent by MSS_i to MSS_j when MSS_i learns that a mobile host
 * has moved from MSS_j cell to its own cell. See the hand-off procedure.
 */
public class BeginHandoff extends Message {

	public MobileHost mh;
	public MobileSupportStation newMSS;
	
	public BeginHandoff(MobileHost mh, MobileSupportStation newMSS) {
		this.mh = mh;
		this.newMSS = newMSS;
	}
	
	@Override
	public Message clone() {
		return new BeginHandoff(mh, newMSS);
	}

}
