package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

// Such a message is sent by a mobile host to its base station to initiate a consensus.
public class Init1 extends Message {

	@Override
	public Message clone() {
		return new Init1();
	}

	@Override
	public String toString() {
		return "INIT_1";
	}
}
