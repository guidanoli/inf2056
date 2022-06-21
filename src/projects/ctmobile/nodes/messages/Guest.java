package projects.ctmobile.nodes.messages;

import projects.ctmobile.nodes.nodeImplementations.MobileHost;
import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
import sinalgo.nodes.messages.Message;

/**
 * Such a message is sent by a mobile host to inform the current base station
 * when it enters a new cell. See the hand-off procedure.
 */
public class Guest extends Message {

	public MobileHost mh;
	public MobileSupportStation oldMSS;
	
	public Guest(MobileHost mh, MobileSupportStation oldMSS) {
		this.mh = mh;
		this.oldMSS = oldMSS;
	}
	
	@Override
	public Message clone() {
		return new Guest(mh, oldMSS);
	}
	
	@Override
	public String toString() {
		return "GUEST(" + mh + ", " + oldMSS + ")";
	}
}
