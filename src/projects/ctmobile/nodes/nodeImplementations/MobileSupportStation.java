package projects.ctmobile.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import projects.ctmobile.LogL;
import projects.ctmobile.nodes.messages.BeginHandoff;
import projects.ctmobile.nodes.messages.Decide;
import projects.ctmobile.nodes.messages.Estimate;
import projects.ctmobile.nodes.messages.Guest;
import projects.ctmobile.nodes.messages.Init1;
import projects.ctmobile.nodes.messages.Init2;
import projects.ctmobile.nodes.messages.Init3;
import projects.ctmobile.nodes.messages.Propose;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

public class MobileSupportStation extends Node {

	/**
	 * Static variables
	 */
	
	// all mobile support station nodes
	private static Vector<MobileSupportStation> allMSSs = new Vector<MobileSupportStation>();
	
	/**
	 * Instance variables
	 */
	
	// Set containing the identities of the mobile hosts located in the cell of MSS_i.
	private HashSet<MobileHost> localMHs;
	
	// Sequence number which identifies the current round executed by MSS_i.
	private int r;
	
	// Phase number in a round. When the protocol starts or ends, Phase_i is equal to 0.
	// Otherwise this variable is either equal to 1, 2, 3 or 4.
	private int phase;

	enum State {
		Undecided,
		Decided,
	}
	
	// This variable is set to decided if the consensus has terminated.
	// Otherwise it is set to undecided.
	private State state;
	
	// Sequence number of the last round during which a new estimate sent by a coordinator
	// has been accepted as the new value of V_i.
	private int ts;
	
	// Set containing the identities of the mobile hosts whose initial values are already known by MSS_i.
	// MSS_i collects values of the mobile hosts located in its cell until endCollect = true.
	private HashSet<MobileHost> p;
	
	// Set containing the collected values.
	private HashSet<Integer> newV;
	
	// Last set of values proposed by MSS_i.
	private HashSet<Integer> v;
	
	// Set containing the estimates received by the coordinator MSS_i during the round r.
	private HashMap<Integer, HashSet<Estimate>> log;
	
	// Number of positive acknowledgments received by the coordinator MSS_i during round r.
	private HashMap<Integer, Integer> nbP;

	// Number of negative acknowledgments received by the coordinator MSS_i during round r.
	private HashMap<Integer, Integer> nbN;
	
	// If MSS_i should stop collecting proposals from mobile hosts
	private boolean endCollect;

	// Inbox for messages sent this node to itself
	private ArrayList<Message> myInbox;
	
	// Outbox for messages sent this node by itself
	private ArrayList<Message> myOutbox;
	
	// Logger for logging events
	private Logging logger = Logging.getLogger("ctmobile.log");

	public void loggedSend(Message m, Node target) {
		assert(target != this);
		send(m, target);
		logger.logln(LogL.MSS, this + " sent " + m + " to " + target);
	}

	public void loggedSendDirect(Message m, Node target) {
		if (target == this) {
			myOutbox.add(m);
		} else {
			sendDirect(m, target);
		}
		logger.logln(LogL.MSS, this + " sent " + m + " directly to " + target);
	}
	
	// Broadcasts to all MSSs
	public void loggedWiredBroadcast(Message msg) {
		for (MobileSupportStation mss : allMSSs) {
			if (mss != this) {
				loggedSendDirect(msg, mss);
			}
		}
	}
	
	// Broadcasts to all local MHs
	public void loggedWirelessBroadcast(Message msg) {
		for (MobileHost mh : localMHs) {
			loggedSend(msg, mh);
		}
	}
	
	public void loggedSendToMSSc(Message msg) {
		loggedSendDirect(msg, getMSSc());
	}
	
	public MobileSupportStation getMSSc() {
		int c = r % allMSSs.size();
		return allMSSs.elementAt(c);
	}
	
	public void loggedPhaseChange(int newPhase) {
		logger.logln(LogL.MSS_PHASES, this + " changed phase: " + phase + " -> " + newPhase);
		phase = newPhase;
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		// messages from other nodes
		for (Message msg : inbox) {
			logger.logln(LogL.MSS, this + " received " + msg);
			handleMessage(msg);
		}
		
		// messages from itself
		for (Message msg : myInbox) {
			logger.logln(LogL.MSS, this + " received " + msg);
			handleMessage(msg);
		}
	}
	
	private void handleMessage(Message msg) {
		if (msg instanceof Guest) {
			handleGuestMessage((Guest)msg);
		} else if (msg instanceof BeginHandoff) {
			handleBeginHandoffMessage((BeginHandoff)msg);
		} else if (msg instanceof Init1 || msg instanceof Init2) {
			handleInit1AndInit2Messages();
		} else if (msg instanceof Propose) {
			handleProposeMessage((Propose)msg);
		}
	}

	private boolean canEndCollection() {
		return p.size() == MobileHost.getNumberOfInstances();
	}

	public void loggedEndCollectChange(boolean newValue) {
		logger.logln(LogL.MSS_END_COLLECT, this + " changed value of EndCollect : " + endCollect + " -> " + newValue);
		endCollect = newValue;
	}
	
	private void handleProposeMessage(Propose msg) {
		if (!endCollect) {
			logger.logln(LogL.MSS_PROPOSE, this + " added " + msg.mh + " to its P set");
			p.add(msg.mh);
			logger.logln(LogL.MSS_PROPOSE, this + ".p = " + p + " (size=" + p.size() + ")");
			
			logger.logln(LogL.MSS_PROPOSE, this + " added value " + msg.v + " to its New_V set");
			newV.add(msg.v);
			logger.logln(LogL.MSS_PROPOSE, this + ".newV = " + newV);
			
			if (canEndCollection()) {
				loggedEndCollectChange(false);
			}
			
			if (phase > 1) {
				loggedSendToMSSc(new Estimate(this, r, newV, p, ts));
			}
		}
	}

	private void handleInit1AndInit2Messages() {
		if (phase == 0) {
			loggedWiredBroadcast(new Init2());
			loggedPhaseChange(1);
			if (!localMHs.isEmpty() && !endCollect) {
				loggedWirelessBroadcast(new Init3());
			}
		}
	}

	private void handleBeginHandoffMessage(BeginHandoff msg) {
		// Hand-off procedure (3)
		logger.logln(LogL.HANDOFF, this + " removed " + msg.mh + " from its local mobile host set");
		localMHs.remove(msg.mh);
		logger.logln(LogL.HANDOFF, this + ".localMHs = " + localMHs);
	}

	private void handleGuestMessage(Guest msg) {
		// Hand-off procedure (2)
		logger.logln(LogL.HANDOFF, this + " added " + msg.mh + " to its local mobile host set");
		localMHs.add(msg.mh);
		logger.logln(LogL.HANDOFF, this + ".localMHs = " + localMHs);
		
		if (msg.oldMSS != null) {
			loggedSendDirect(new BeginHandoff(msg.mh, this), msg.oldMSS);
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
		nbP = new HashMap<Integer, Integer>();
		nbP.put(r, 0);
		nbN = new HashMap<Integer, Integer>();
		nbN.put(r, 0);
		endCollect = false;
		myInbox = new ArrayList<Message>();     // Inbox for messages from itself (read-only)
		myOutbox = new ArrayList<Message>();    // Inbox for messages to itself (append-only)
	}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {
		// Swap boxes and clear outbox
		ArrayList<Message> tmp = myInbox;
		myInbox = myOutbox;
		myOutbox = tmp;
		myOutbox.clear();
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
