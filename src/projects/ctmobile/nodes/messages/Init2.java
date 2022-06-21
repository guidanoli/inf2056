package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

// When a base station is asked by a mobile host to initiate a consensus,
// it broadcasts this message to inform the other base stations that a consensus
// is started. To ensure a reliable broadcast of message INIT-2, each destination
// base station has to forward it to the other base stations. So, despite failures
// of base stations, all (or none) correct base stations will be aware that a
// consensus has been initiated.
public class Init2 extends Message {

	@Override
	public Message clone() {
		return new Init2();
	}

	@Override
	public String toString() {
		return "INIT_2";
	}
}
