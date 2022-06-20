package projects.ctmobile.nodes.messages;

import sinalgo.nodes.messages.Message;

// This message is sent to a mobile host either when its base station is informed
// (on receipt of INIT-2 message) that a consensus has started or when the mobile
// host enters a new cell managed by a base station MSSi which is not aware of its
// initial value and has not yet completed its collection of values (|Pi| < alpha)
public class Init3 extends Message {

	@Override
	public Message clone() {
		return new Init3();
	}

}
