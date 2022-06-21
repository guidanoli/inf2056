package projects.ctmobile.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import projects.ctmobile.LogL;
import projects.ctmobile.nodes.messages.BeginHandoff;
import projects.ctmobile.nodes.messages.Decide;
import projects.ctmobile.nodes.messages.Estimate;
import projects.ctmobile.nodes.messages.Guest;
import projects.ctmobile.nodes.messages.Init3;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

public class MobileSupportStation extends Node {

	// all mobile support station nodes
	private static Vector<MobileSupportStation> allMSSs = new Vector<MobileSupportStation>();
	
	// Set containing the identities of the mobile hosts located in the cell of MSS_i.
	HashSet<MobileHost> localMHs;
	
	// Sequence number which identifies the current round executed by MSS_i.
	int r;
	
	// Phase number in a round. When the protocol starts or ends, Phase_i is equal to 0.
	// Otherwise this variable is either equal to 1, 2, 3 or 4.
	int phase;

	enum State {
		Undecided,
		Decided,
	}
	
	// This variable is set to decided if the consensus has terminated.
	// Otherwise it is set to undecided.
	State state;
	
	// Sequence number of the last round during which a new estimate sent by a coordinator
	// has been accepted as the new value of V_i.
	int ts;
	
	// Set containing the identities of the mobile hosts whose initial values are already known by MSS_i.
	// MSS_i collects values of the mobile hosts located in its cell until endCollect = true.
	HashSet<MobileHost> p;
	
	// Set containing the collected values.
	HashSet<Integer> newV;
	
	// Last set of values proposed by MSS_i.
	HashSet<Integer> v;
	
	// Set containing the estimates received by the coordinator MSS_i during the round r.
	HashMap<Integer, HashSet<Estimate>> log;
	
	// Number of positive acknowledgments received by the coordinator MSS_i during round r.
	HashMap<Integer, Integer> nPositive;

	// Number of negative acknowledgments received by the coordinator MSS_i during round r.
	HashMap<Integer, Integer> nNegative;
	
	// If MSS_i should stop collecting proposals from mobile hosts
	boolean endCollect;
	
	private Logging logger = Logging.getLogger("ctmobile.log");
	
	@Override
	public void handleMessages(Inbox inbox) {
		for (Message msg : inbox) {
			logger.logln(LogL.MSS, this + " received " + msg);
			if (msg instanceof Guest) {
				handleGuestMessage((Guest)msg);
			} else if (msg instanceof BeginHandoff) {
				handleBeginHandoffMessage((BeginHandoff)msg);
			}
		}
	}

	public void loggedSend(Message m, Node target) {
		send(m, target);
		logger.logln(LogL.MSS, this + " sent " + m + " to " + target);
	}
	
	private void handleBeginHandoffMessage(BeginHandoff msg) {
		localMHs.remove(msg.mh);
	}

	private void handleGuestMessage(Guest msg) {
		localMHs.add(msg.mh);
		if (msg.oldMSS != null) {
			sendDirect(new BeginHandoff(msg.mh, this), msg.oldMSS);
		}
		if (phase != 0 && !p.contains(msg.mh) && !endCollect) {
			loggedSend(new Init3(), msg.mh);
		}
		if (phase == 0 && state == State.Decided) {
			loggedSend(new Decide(v), msg.mh);
		}
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}
	
	public int getIndex() {
		return allMSSs.indexOf(this) + 1;
	}
	
	@Override
	public String toString() {
		return "MSS_" + getIndex();
	}

	@Override
	public void init() {
		allMSSs.add(this);
		localMHs = new HashSet<MobileHost>();
		r = 0;
		phase = 0;
		state = State.Undecided;
		ts = 0;
		p = new HashSet<MobileHost>();
		newV = new HashSet<Integer>();
		v = new HashSet<Integer>();
		log = new HashMap<Integer, HashSet<Estimate>>();
		log.put(r, new HashSet<Estimate>());
		nPositive = new HashMap<Integer, Integer>();
		nPositive.put(r, 0);
		nNegative = new HashMap<Integer, Integer>();
		nNegative.put(r, 0);
		endCollect = false;
	}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.setColor(Color.getHSBColor(.33f, 1.f, .39f));
		String text = Integer.toString(getIndex());
		this.drawNodeAsSquareWithText(g, pt, highlight, text, 24, Color.WHITE);
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
