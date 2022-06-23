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
import projects.ctmobile.nodes.messages.NA;
import projects.ctmobile.nodes.messages.NewEstimate;
import projects.ctmobile.nodes.messages.PA;
import projects.ctmobile.nodes.messages.Propose;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class MobileSupportStation extends Node {

	/**
	 * Static variables
	 */
	
	// all mobile support station nodes
	public static Vector<MobileSupportStation> allMSSs = new Vector<MobileSupportStation>();
	
	private static int nRoundsToSuspect;
	{
		try {
			nRoundsToSuspect = Configuration.getIntegerParameter("ctmobile/MobileSupportStation/nRoundsToSuspect");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
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
	public HashSet<MobileHost> p;
	
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
	
	// Number of rounds in phase 3
	private int roundsInPhase3;
	
	// Logger for logging events
	private Logging logger = Logging.getLogger("ctmobile.log");

	private void loggedSend(Message m, Node target) {
		assert(target != this);
		send(m, target);
		logger.logln(LogL.MSS, this + " sent " + m + " to " + target);
	}

	private void loggedSendDirect(Message m, Node target) {
		if (target == this) {
			myOutbox.add(m);
		} else {
			sendDirect(m, target);
		}
		logger.logln(LogL.MSS, this + " sent " + m + " directly to " + target);
	}
	
	// Broadcasts to all MSSs
	private void loggedWiredBroadcast(Message msg, boolean includeItself) {
		for (MobileSupportStation mss : allMSSs) {
			if ((mss != this) || includeItself) {
				loggedSendDirect(msg, mss);
			}
		}
	}
	
	// Broadcasts to all local MHs
	private void loggedWirelessBroadcast(Message msg) {
		for (MobileHost mh : localMHs) {
			loggedSend(msg, mh);
		}
	}
	
	private void loggedSendToMSSc(Message msg) {
		loggedSendDirect(msg, getMSSc());
	}
	
	private MobileSupportStation getMSSc() {
		int c = r % allMSSs.size();
		return allMSSs.elementAt(c);
	}
	
	private void loggedPhaseChange(int newPhase) {
		int oldPhase = phase;
		phase = newPhase;
		logger.logln(LogL.MSS_PHASES, this + " changed phase: " + oldPhase + " -> " + newPhase);
	}
	
	private void loggedAddP(MobileHost mh) {
		p.add(mh);
		logger.logln(LogL.MSS_P_SET, this + " added node " + mh + " to its P set");
		logger.logln(LogL.MSS_P_SET, this + ".p = " + p + " (size=" + p.size() + ")");
	}
	
	private void loggedAddPs(HashSet<MobileHost> mhs) {
		p.addAll(mhs);
		logger.logln(LogL.MSS_P_SET, this + " added nodes " + mhs + " to its P set");
		logger.logln(LogL.MSS_P_SET, this + ".p = " + p + " (size=" + p.size() + ")");
	}
	
	private void loggedAddVToNewV(Integer vToAdd) {
		newV.add(vToAdd);
		logger.logln(LogL.MSS_V_SET, this + " added value " + vToAdd + " to its New_V set");
		logger.logln(LogL.MSS_V_SET, this + ".newV = " + newV);
	}

	private void loggedAddVsToNewV(HashSet<Integer> vsToAdd) {
		newV.addAll(vsToAdd);
		logger.logln(LogL.MSS_V_SET, this + " added values " + vsToAdd + " to its New_V set");
		logger.logln(LogL.MSS_V_SET, this + ".newV = " + newV);
	}
	
	private void loggedUpdateV(HashSet<Integer> vToOverwrite) {
		v = new HashSet<Integer>(vToOverwrite);
		logger.logln(LogL.MSS_V_SET, this + " updated its V set to " + vToOverwrite);
	}

	private void loggedStateChange(State newState) {
		State oldState = state;
		state = newState;
		logger.logln(LogL.MSS_STATE, this + " changed its state: " + oldState + " -> " + newState);
	}

	private void loggedTimestampChange(int newTs) {
		int oldTs = ts;
		ts = newTs;
		logger.logln(LogL.MSS_TS, this + " changed its timestamp: " + oldTs + " -> " + newTs);
	}

	private void loggedEndCollectChange(boolean newValue) {
		boolean oldValue = endCollect;
		endCollect = newValue;
		logger.logln(LogL.MSS_END_COLLECT, this + " changed value of EndCollect : " + oldValue + " -> " + newValue);
	}

	private void loggedNextRound() {
		r = r + 1;
		initRound(r);
		logger.logln(LogL.MSS_ROUND, this + " is now on round " + r);
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
		} else if ((msg instanceof Init1) || (msg instanceof Init2)) {
			handleInit1AndInit2Messages();
		} else if (msg instanceof Propose) {
			handleProposeMessage((Propose)msg);
		} else if (msg instanceof Decide) {
			handleDecideMessage((Decide)msg);
		} else if (msg instanceof Estimate) {
			handleEstimateMessage((Estimate)msg);
		} else if (msg instanceof PA) {
			handlePositiveAckMessage((PA)msg);
		} else if (msg instanceof NA) {
			handleNegativeAckMessage((NA)msg);
		} else if (msg instanceof NewEstimate) {
			handleNewEstimativeMessage((NewEstimate)msg);
		}
	}
	
	private void handleNewEstimativeMessage(NewEstimate msg) {
		// Action 12
		if (phase == 3) {
			if (msg.endCollect) {
				loggedUpdateV(msg.v);
				loggedTimestampChange(msg.r);
				loggedEndCollectChange(true);
				loggedSendToMSSc(new PA(this, msg.r));
			} else {
				loggedSendToMSSc(new NA(this, msg.r));
				loggedAddPs(msg.p);
				loggedAddVsToNewV(msg.v);
				if (hasEnoughProposals()) {
					loggedEndCollectChange(true);
				}
			}
			if (getMSSc() == this) {
				loggedPhaseChange(4);
			} else {
				loggedPhaseChange(1);
			}
		}
	}

	private void handlePositiveAckMessage(PA msg) {
		// Action 8
		int round = msg.r;
		initRound(round);
		nbP.put(round, nbP.get(round) + 1);
		logger.logln(LogL.MSS_ACKS, this + " received a positive ack");
		logger.logln(LogL.MSS_ACKS, this + " votes for round " + round + ": " +
				nbP.get(round) + " pos / " + nbN.get(round) + " neg");
	}

	private void handleNegativeAckMessage(NA msg) {
		// Action 9 
		int round = msg.r;
		initRound(round);
		nbN.put(round, nbN.get(round) + 1);
		logger.logln(LogL.MSS_ACKS, this + " received a negative ack");
		logger.logln(LogL.MSS_ACKS, this + " votes for round " + round + ": " +
				nbP.get(round) + " pos / " + nbN.get(round) + " neg");
	}
	
	/**
	 * The statement Log_c[r] := Log_c[r] @ (MSS_i,r, V_i,ts_i) executed in action 7 is equivalent to two
	 * successive operations: (1) Log_c[r] := Log_c[r] U { (MSS_i,r,V_i',ts_i)} and (2) if there exists
	 * (MSS_i,r,V_i',ts_i) E Log_c[r] and (MSS_i,r,V_i'',ts_i) E Log_c[r] such that card(V_i') <= card(V_i"),
	 * then (MSS_i,r,V_i', ts_i) is removed from Log_c[r].
	 */
	private void addEstimateToLog(Estimate newE) {
		initRound(newE.r);
		HashSet<Estimate> estimates = log.get(newE.r);
		HashSet<Estimate> toRemove = new HashSet<Estimate>();
		for (Estimate e : estimates) {
			if ((e.mss == newE.mss) &&
				(e.r == newE.r) &&
				(e.ts == newE.ts))
			{
				if (e.v.size() <= newE.v.size()) {
					// By definition of the @ operator,
					// this should be removed
					toRemove.add(e);
				} else {
					// has estimate with set of values
					// with higher or the same cardinality
					return;
				}
			}
		}
		estimates.add(newE);
		estimates.removeAll(toRemove);
		logger.logln(LogL.MSS_LOG, this + " added estimate " + newE + " to its log of round " + newE.r);
		logger.logln(LogL.MSS_LOG, this + ".log[" + newE.r + "] = " + estimates);
	}
	
	private void handleEstimateMessage(Estimate msg) {
		// Action 7
		addEstimateToLog(msg);
		if (!endCollect) {
			loggedAddPs(msg.p);
			loggedAddVsToNewV(msg.v);
			if (hasEnoughProposals()) {
				loggedEndCollectChange(true);
			}
		}
	}

	private void handleDecideMessage(Decide msg) {
		// Action 6
		if (state == State.Undecided) {
			loggedStateChange(State.Decided);
			loggedUpdateV(msg.v);
			loggedWiredBroadcast(msg, false);
			loggedWirelessBroadcast(msg);
			loggedPhaseChange(0);
		}
	}

	private boolean hasEnoughProposals() {
		return p.size() == MobileHost.getNumberOfInstances();
	}

	private void handleProposeMessage(Propose msg) {
		// Action 5
		if (!endCollect) {
			loggedAddP(msg.mh);
			loggedAddVToNewV(msg.v);
			if (hasEnoughProposals()) {
				loggedEndCollectChange(true);
			}
			if (phase > 1) {
				loggedSendToMSSc(new Estimate(this, r, newV, p, ts));
			}
		}
	}

	private void handleInit1AndInit2Messages() {
		// Action 4
		if (phase == 0 && state == State.Undecided) {
			loggedWiredBroadcast(new Init2(), false);
			loggedPhaseChange(1);
			if ((!localMHs.isEmpty()) && (!endCollect)) {
				loggedWirelessBroadcast(new Init3());
			}
		}
	}

	private void handleBeginHandoffMessage(BeginHandoff msg) {
		// Hand-off procedure (3)
		localMHs.remove(msg.mh);
		logger.logln(LogL.HANDOFF, this + " removed " + msg.mh + " from its local mobile host set");
		logger.logln(LogL.HANDOFF, this + ".localMHs = " + localMHs);
	}

	private void handleGuestMessage(Guest msg) {
		// Hand-off procedure (2)
		localMHs.add(msg.mh);
		logger.logln(LogL.HANDOFF, this + " added " + msg.mh + " to its local mobile host set");
		logger.logln(LogL.HANDOFF, this + ".localMHs = " + localMHs);
		
		if (msg.oldMSS != null) {
			loggedSendDirect(new BeginHandoff(msg.mh, this), msg.oldMSS);
		}
		if ((phase != 0) && (!p.contains(msg.mh)) && (!endCollect)) {
			loggedSend(new Init3(), msg.mh);
		}
		if ((phase == 0) && (state == State.Decided)) {
			loggedSend(new Decide(v), msg.mh);
		}
	}

	@Override
	public void preStep() {
		if (phase == 3) {
			roundsInPhase3++;
		} else {
			roundsInPhase3 = 0;
		}
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
		nbP = new HashMap<Integer, Integer>();
		nbN = new HashMap<Integer, Integer>();
		endCollect = false;
		myInbox = new ArrayList<Message>();     // Inbox for messages from itself (read-only)
		myOutbox = new ArrayList<Message>();    // Inbox for messages to itself (append-only)
		roundsInPhase3 = 0;
		initRound(r);
	}

	@Override
	public void neighborhoodChange() {}

	private static double majorityOfMSSs() {
		int n = allMSSs.size();
		return n / 2.0;
	}
	
	private void initRound(int round) {
		log.putIfAbsent(round, new HashSet<Estimate>());
		nbP.putIfAbsent(round, 0);
		nbN.putIfAbsent(round, 0);
	}
	
	@Override
	public void postStep() {
		// Action 10
		if (phase == 1) {
			loggedNextRound();
			if (ts == 0) {
				loggedUpdateV(newV);
			}
			loggedSendToMSSc(new Estimate(this, r, v, p, ts));
			if (getMSSc() == this) {
				loggedPhaseChange(2);
			} else {
				loggedPhaseChange(3);
			}
		}
		
		// Action 11
		if ((phase == 2) && (log.get(r).size() > majorityOfMSSs())) {
			int tsMax = 0;
			Estimate eMax = null;
			for (Estimate e : log.get(r)) {
				if (e.ts >= tsMax) {
					tsMax = e.ts;
					eMax = e;
				}
			}
			if (tsMax > 0) {
				assert(eMax != null);
				loggedUpdateV(eMax.v);
			} else {
				loggedUpdateV(newV);
			}
			loggedWiredBroadcast(new NewEstimate(this, r, v, p, endCollect), true);
			loggedPhaseChange(3);
		}
		
		// Action 13
		if ((phase == 3) && (roundsInPhase3 >= nRoundsToSuspect)) {
			loggedSendToMSSc(new NA(this, r));
			phase = 1;
		}
		
		// Action 14
		if ((phase == 4) && ((nbP.get(r) + nbN.get(r)) > majorityOfMSSs())) {
			if (nbP.get(r) > majorityOfMSSs()) {
				loggedWiredBroadcast(new Decide(v), false);
				loggedStateChange(State.Decided);
				loggedPhaseChange(0);
			} else {
				loggedPhaseChange(1);
			}
		}
		
		// Swap boxes and clear outbox
		ArrayList<Message> tmp = myInbox;
		myInbox = myOutbox;
		myOutbox = tmp;
		myOutbox.clear();
	}
	
	private static int radius;
	{
		try {
			radius = Configuration.getIntegerParameter("UDG/rMax");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		if (getMSSc() == this) {
			this.setColor(Color.BLUE);
		} else {
			this.setColor(Color.getHSBColor(.33f, 1.f, .39f));
		}
		String text = Integer.toString(phase);
		this.drawNodeAsSquareWithText(g, pt, highlight, text, 24, Color.WHITE);
		pt.translateToGUIPosition(this.getPosition());
		int ovalR = (int) (radius * pt.getZoomFactor());
		g.setColor(Color.GRAY);
		g.drawOval(pt.guiX - ovalR, pt.guiY - ovalR, ovalR*2, ovalR*2);
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
