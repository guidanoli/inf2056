/**
 * BSD 3-Clause License
 * 
 * Copyright (c) 2022, Guilherme Dantas
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package projects.sanders.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import projects.sanders.nodes.messages.InquireMessage;
import projects.sanders.nodes.messages.ReleaseMessage;
import projects.sanders.nodes.messages.RelinquishMessage;
import projects.sanders.nodes.messages.RequestMessage;
import projects.sanders.nodes.messages.SandersMessage;
import projects.sanders.nodes.messages.YesMessage;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;
import sinalgo.tools.statistics.Distribution;

/**
 * Node that implements the distributed mutual exclusion algorithm specified on (Sanders, 1987).
 * @author Guilherme Dantas
 */
public class SandersNode extends Node {
	
	// --------------------------------------------------------------------------------------------
	// State variables
	// --------------------------------------------------------------------------------------------

	/**
	 * All the possible states a node can be in
	 * @author Guilherme Dantas
	 */
	public enum State {
		MappingDistrict,  // still establishing connection to nodes in district
		NotInCS,          // node is not in the critical section
		Waiting,          // node is waiting for approvals to get into the critical section
		InCS              // node is in the critical section
	}
	
	// Get random number generator singleton from framework
	private static Random rand = Distribution.getRandom();
	
	// Outbox of delayed messages
	private ArrayList<DelayedMessage> delayedOutbox;
	
	// Number of rounds left to delay the sending of messages
	private int nDelayedRoundsLeft;
	
	// Number of rounds to delay messages
	private static final int nDelayRounds = 2;
	
	// The state of the node 
	private State state;
	
	// Current logical clock (updated every new received message)
	private int currTS;
	
	// Time stamp of the last request message sent by this node
	private int myTS;
	
	// Number of nodes that answered this node's request with a yes
	private int yesVotes;
	
	// Flag indicating that this node has sent a yes to some other node
	// and have not received a release message from it yet
	private boolean hasVoted;
	
	// The request message that was last replied with a yes from this node
	// or null if this node has not sent a yes yet
	private RequestMessage candMsg;
	
	// Flag indicating that this node has tried to inquire its yes
	private boolean inquired;
	
	// Request message comparator
	private RequestMessageComparator reqMsgComp;

	// Priority queue of requests ordered by time stamp and node IDs such that
	// removing the message with the lowest time stamp is an easy operation
	private PriorityQueue<RequestMessage> deferredQ;

	// District (set of node IDs) associated with this node
	private HashSet<Integer> district;
	
	// Probability that a node will delay the delivery of a message
	private double pDelay;
	
	// Probability that a node will request access to the CS
	private double pRequest;

	// Probability that a node will exit the CS and send a release message
	private double pRelease;
	
	// Inbox for messages sent this node to itself
	private ArrayList<Message> myInbox;
	
	// Outbox for messages sent this node by itself
	private ArrayList<Message> myOutbox;
	
	// --------------------------------------------------------------------------------------------
	// Initialization
	// --------------------------------------------------------------------------------------------

	/**
	 * Initialize node
	 */
	@Override
	public void init() {
		state = State.MappingDistrict;          // Nodes start mapping their district
		currTS = myTS = 1;                      // All time stamps start with 1
		yesVotes = 0;                           // No one has voted yet
		hasVoted = false;                       // No one has voted yet
		inquired = false;                       // No one has sent an inquiry message yet
		candMsg = null;                         // No one has sent a request message yet
		district = getDistrict();               // Get districts from configuration file
		pDelay = getDelayProbability();         // Get delay probability from configuration file 
		pRequest = getRequestProbability();     // Get request probability from configuration file
		pRelease = getReleaseProbability();     // Get release probability from configuration file
		myInbox = new ArrayList<Message>();     // Inbox for messages from itself (read-only)
		myOutbox = new ArrayList<Message>();    // Inbox for messages to itself (append-only)
		
		// create empty priority queue with special comparator
		reqMsgComp = new RequestMessageComparator();
		deferredQ = new PriorityQueue<RequestMessage>(reqMsgComp);
		
		// no delayed messages
		nDelayedRoundsLeft = 0;
		delayedOutbox = new ArrayList<DelayedMessage>();
	}
	
	/**
	 * Create a set of node IDs that are in the district of this node
	 * The configuration file is read and if "sanders/s{ID}" is missing,
	 * a message will pop up in the GUI alerting of this fatal error.
	 * @return set of node IDs
	 */
	private HashSet<Integer> getDistrict() {
		HashSet<Integer> district = new HashSet<Integer>();
		try {
			String districtString = Configuration.getStringParameter("sanders/s" + this.ID);
			String[] idStringArray = districtString.split(",");
			for (String idString : idStringArray) {
				int id = Integer.parseInt(idString);
				district.add(id);
			}
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
		}
		return district;
	}
	
	/**
	 * Read the probability of delay from configuration
	 * @return probability of delay
	 */
	private double getDelayProbability() {
		try {
			return Configuration.getDoubleParameter("sanders/pdelay");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
			return 0.0;
		}
	}
	
	/**
	 * Read the probability of request from configuration
	 * @return probability of a node requesting access to CS
	 */
	private double getRequestProbability() {
		try {
			return Configuration.getDoubleParameter("sanders/prequest");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Read the probability of release from configuration
	 * @return probability of a node exiting the CS
	 */
	private double getReleaseProbability() {
		try {
			return Configuration.getDoubleParameter("sanders/prelease");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
			return 0.0;
		}
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// pDelay ∈ [0,1]
		if (!(pDelay >= 0.0 && pDelay <= 1.0)) {
			throw new WrongConfigurationException("pDelay out of range");
		}
		// pRequest ∈ [0,1]
		if (!(pRequest >= 0.0 && pRequest <= 1.0)) {
			throw new WrongConfigurationException("pRequest out of range");
		}
		// pRelease ∈ [0,1]
		if (!(pRelease >= 0.0 && pRelease <= 1.0)) {
			throw new WrongConfigurationException("pRelease out of range");
		}
		// i ∈ Si
		if (!district.contains(ID)) {
			throw new WrongConfigurationException("node's district doesn't contain itself");
		}
		// Si ∩ Sj is non-empty
		for (Edge e : this.outgoingConnections) {
			Node node = e.endNode;
			if (node instanceof SandersNode) {
				SandersNode neighbour = (SandersNode) node;
				HashSet<Integer> nbDistrict = neighbour.district;
				boolean hasNodeInCommon = false;
				for (Integer i : district) {
					if (nbDistrict.contains(i)) {
						hasNodeInCommon = true;
						break;
					}
				}
				if (!hasNodeInCommon) {
					throw new WrongConfigurationException("nodes " + ID + " and " + neighbour.ID +
							                              " have disjoint districts");
				}
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Critical section methods
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Ask permission to nodes in district before entering the CS
	 */
	private void enterCS() {
		assert(state == State.NotInCS);
		state = State.Waiting;
		myTS = currTS; // save the time stamp of the request
		broadcastToDistrict(new RequestMessage(myTS, this));
	}
	
	/**
	 * Notify nodes in district that it has left the CS
	 */
	private void exitCS() {
		assert(state == State.InCS);
		state = State.NotInCS;
		yesVotes = 0;
		broadcastToDistrict(new ReleaseMessage(currTS, this));
	}

	// --------------------------------------------------------------------------------------------
	// Message handlers
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Wrapped send method (handles messages to itself)
	 * @param msg
	 * @param target
	 */
	private void sendSandersMessage(SandersMessage msg, Node target) {
		if (nDelayedRoundsLeft == 0) {
			sendSandersMessageNow(msg, target);
		} else {
			delayedOutbox.add(new DelayedMessage(msg, target));
		}
	}
	
	/**
	 * Wrapped send method (handles messages to itself) without
	 * checking if it needs to be delayed
	 * @note don't call this method directly
	 * @param msg
	 * @param target
	 */
	private void sendSandersMessageNow(SandersMessage msg, Node target) {
		if (target == this) {
			// Sinalgo doesn't support message sending to itself,
			// so we simulate it by having an outbox for itself
			myOutbox.add(msg);
		} else {
			// If the target is not itself, use Sinalgo's usual way
			send(msg, target);
		}
	}
	
	/**
	 * Handle messages
	 */
	@Override
	public void handleMessages(Inbox inbox) {
		/**
		 * Handle messages from other nodes
		 */
		for (Message msg : inbox) {
			if (msg instanceof SandersMessage) {
				handleSandersMessage((SandersMessage) msg);
			} else {
				Global.log.logln("Received unexpected message " + msg);
			}
		}
		/**
		 * If sinalgo.nodes.Node#preStep() added any messages to itself,
		 * we'll only handle them after all the messages in the inbox
		 * of messages from other nodes 
		 */
		for (Message msg : myInbox) {
			if (msg instanceof SandersMessage) {
				handleSandersMessage((SandersMessage) msg);
			} else {
				Global.log.logln("Received unexpected message " + msg);
			}
		}
		/**
		 * sinalgo.nodes.Node#postStep() will:
		 * - move myNextInbox to myCurrentInbox
		 * - empty myNextInbox
		 */
	}
	
	/**
	 * Handle message that contains time stamp and sender
	 * @param msg
	 */
	private void handleSandersMessage(SandersMessage msg) {
		// update logical clock
		currTS = Math.max(currTS, msg.ts) + 1;
		
		// handle message by type
		if (msg instanceof RequestMessage) {
			handleRequestMessage((RequestMessage) msg);
		} else if (msg instanceof YesMessage) {
			handleYesMessage((YesMessage) msg);
		} else if (msg instanceof InquireMessage) {
			handleInquireMessage((InquireMessage) msg);
		} else if (msg instanceof RelinquishMessage) {
			handleRelinquishMessage((RelinquishMessage) msg);
		} else if (msg instanceof ReleaseMessage) {
			handleReleaseMessage((ReleaseMessage) msg);
		} else {
			Global.log.logln("Received unexpected message " + msg);
		}
	}

	/**
	 * Handle request message
	 * @param msg
	 */
	private void handleRequestMessage(RequestMessage msg) {
		if (hasVoted) {
			// if this node has already voted for some node, then
			// any further requests will be queued
			deferredQ.add(msg);
			
			// But if the request that just arrived has a higher priority
			// over the current candidate, and this node hasn't inquired
			// its vote yet, then...
			if (reqMsgComp.compare(msg, candMsg) < 0 && !inquired) {
				// it will send an inquiry to its candidate with the same
				// time stamp as its request message
				sendSandersMessage(new InquireMessage(candMsg.ts, this), candMsg.sender);
				
				// and set the "inquired" flag to true as to wait for
				// the "relinquish" reply message
				inquired = true;
			}
		} else {
			// if this node hasn't voted yet, then the request will be served
			serveRequest(msg);
			
			// register that this node has already voted for a candidate already,
			// so that any requests that arrive later won't be readily accepted
			hasVoted = true;
		}
	}
	
	/**
	 * Handle yes message
	 * @param msg
	 */
	private void handleYesMessage(YesMessage msg) {
		// this message is sent by nodes that received this node's request
		// and haven't voted in any other candidate until that point
		if (state == State.Waiting) {
			// we then increase the vote counter
			yesVotes++;
		} else {
			Global.log.logln("Received YES unexpectedly");
		}
	}
	
	/**
	 * Handle inquire message
	 * @param msg
	 */
	private void handleInquireMessage(InquireMessage msg) {
		// this message is sent by a node that accepted this node's request
		// but has received a request with an older time stamp, and now wants
		// their vote back so that they can vote in the other candidate.
		if (state == State.Waiting) {
			// if the node sent the correct time stamp
			if (msg.ts == myTS) {
				// then, send a relinquish message
				sendSandersMessage(new RelinquishMessage(currTS, this), msg.sender);
				// decrease the vote counter
				assert(yesVotes > 0);
				yesVotes--;
			} else {
				Global.log.logln("Received INQUIRE with wrong TS");
			}
		} else {
			// sometimes, a node might send an inquire message but the
			// node already got into the critical section, so it's too late
			// for it to return its vote
		}
	}

	/**
	 * Send a YES to requesting node and register message as the one selected
	 * @param msg
	 */
	private void serveRequest(RequestMessage msg) {
		sendSandersMessage(new YesMessage(currTS, this), msg.sender);
		candMsg = msg;
	}
	
	/**
	 * Handle relinquish message
	 * @param msg
	 */
	private void handleRelinquishMessage(RelinquishMessage msg) {
		deferredQ.add(candMsg);
		serveRequest(deferredQ.remove());
		inquired = false;
	}

	/**
	 * Handle release message
	 * @param msg
	 */
	private void handleReleaseMessage(ReleaseMessage msg) {
		if (deferredQ.isEmpty()) {
			hasVoted = false;
		} else {
			serveRequest(deferredQ.remove());
		}
		inquired = false;
	}

	// --------------------------------------------------------------------------------------------
	// Graphical User Interface
	// --------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "ID: " + ID + "\n" +
				"State: " + state + "\n" +
				"District: " + district + "\n" +
				"Request PQ: " + deferredQ + "\n" +
				"Has voted: " + hasVoted + "\n" +
				"Candidate: " + candMsg + "\n" +
				"Has inquired: " + inquired + "\n" +
				"# of Yes votes: " + yesVotes + "\n" +
				"Current timestamp: " + currTS + "\n" +
				"Timestamp of request: " + myTS;
	}
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#draw(java.awt.Graphics, sinalgo.gui.transformation.PositionTransformation, boolean)
	 */
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		switch (state) {
		case MappingDistrict: // grey (OFF)
			this.setColor(new Color(0.5f, 0.5f, 0.5f));
			break;
		case NotInCS: // black (ON, doing elsewhere)
			this.setColor(new Color(0.0f, 0.0f, 0.0f));
			break;
		case Waiting: // yellow (trying to get in CS)
			this.setColor(new Color(0.8f, 0.67f, 0.0f));
			break;
		case InCS: // green (inside the CS)
			this.setColor(new Color(0.0f, 0.8f, 0.0f));
			break;
		default:
			break;
		}
		super.drawNodeAsDiskWithText(g, pt, highlight, "", 10, Color.YELLOW);
	}

	// --------------------------------------------------------------------------------------------
	// Other methods
	// --------------------------------------------------------------------------------------------
	
	public State getState() {
		return state;
	}
	
	/**
	 * Check if node is in node's district
	 * @param node
	 * @return whether node is in district
	 */
	private boolean isInDistrict(Node node) {
		return district.contains(node.ID);
	}
	
	/**
	 * Get set of nodes in district (including the node itself)
	 * @return district set
	 */
	private HashSet<Node> getDistrictNodes() {
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.add(this); // add itself
		for (Edge e : this.outgoingConnections) {
			Node node = e.endNode;
			if (isInDistrict(node)) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	private void broadcastToDistrict(SandersMessage msg) {
		for (Node nb : getDistrictNodes()) {
			sendSandersMessage(msg, nb);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Abstract methods
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Check if district has been mapped
	 * @return
	 */
	private boolean isDistrictMapped() {
		for (Integer nodeID : district) {
			boolean found = false;
			if (nodeID == ID) {
				// you won't find this node in its
				// outgoing connections, but it is
				// technically mapped
				continue;
			}
			for (Edge e : this.outgoingConnections) {
				Node node = e.endNode;
				if (node.ID == nodeID) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for any significant state change
	 */
	@Override
	public void preStep() {
		// If messages aren't delayed...
		if (nDelayedRoundsLeft == 0) {
			// If there are delayed messages to be sent
			if (!delayedOutbox.isEmpty()) {
				for (DelayedMessage delayedMsg : delayedOutbox) {
					sendSandersMessageNow(delayedMsg.msg, delayedMsg.target);
				}
				// clear delayed messages outbox
				delayedOutbox.clear();
			}
			// Check if messages should be delayed
			if (rand.nextDouble() <= pDelay) {
				nDelayedRoundsLeft = nDelayRounds;
			}
		}
		
		// Check if the state of the node should be changed
		switch (state) {
		case MappingDistrict:
			if (isDistrictMapped()) {
				state = State.NotInCS;
			}
			break;
		case NotInCS:
			if (rand.nextDouble() <= pRequest) {
				enterCS();
			}
			break;
		case Waiting:
			if (yesVotes >= district.size()) {
				state = State.InCS;
			}
			break;
		case InCS:
			if (rand.nextDouble() <= pRelease) {
				exitCS();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void neighborhoodChange() {
		if (state != State.MappingDistrict) {
			assert(isDistrictMapped());
		}
	}

	@Override
	public void postStep() {
		// If messages are being delayed,
		if (nDelayedRoundsLeft > 0) {
			// decrease the counter
			nDelayedRoundsLeft--;
		}
		
		// Swap boxes and clear outbox
		ArrayList<Message> tmp = myInbox;
		myInbox = myOutbox;
		myOutbox = tmp;
		myOutbox.clear();
	}

	// --------------------------------------------------------------------------------------------
	// Nested classes
	// --------------------------------------------------------------------------------------------

	/**
	 * A comparator implementation for ordering request messages
	 * @author Guilherme Dantas
	 */
	public class RequestMessageComparator implements Comparator<RequestMessage> {

		/**
		 * If this method returns a negative value, it means that req1 has a
		 * higher priority over req2, and vice-versa. If it returns zero, it
		 * means they have the same priority (which should never happen in our
		 * case since it would mean that both messages have the same sender and
		 * time stamp).
		 */
		public int compare(RequestMessage msg1, RequestMessage msg2) {
			if (msg1.ts == msg2.ts) {
				return msg1.sender.ID - msg2.sender.ID;
			} else {
				return msg1.ts - msg2.ts;
			}
		}

	}
	
	/**
	 * Simple class for delayed messages
	 * @author Guilherme Dantas
	 */
	public class DelayedMessage {
		public SandersMessage msg;
		public Node target;
		
		public DelayedMessage(SandersMessage msg, Node target) {
			this.msg = msg;
			this.target = target;
		}
	}
}
