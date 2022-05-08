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

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import projects.sanders.nodes.messages.RequestMessage;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Main;
import sinalgo.tools.logging.Logging;

/**
 * Node that implements the distributed mutual exclusion algorithm specified on (Sanders, 1987).
 * @author Guilherme Dantas
 */
public class SandersNode extends Node {
	
	// Flag indicating that this node is in the CS
	private boolean inCS;
	
	// Current logical clock
	private int currTS;
	
	// Time stamp of this node's request message
	private int myTS;
	
	// Number of nodes that answered this node's request with a yes
	private int yesVotes;
	
	// Flag indicating that this node has sent a yes to some other node
	// and have not received a release message from it yet
	private boolean hasVoted;
	
	// The node to whom this node has last sent a yes after receiving
	// a request message from it (or null if there is no such node)
	private SandersNode cand;
	
	// Time stamp of the request message sent by the node to whom this node
	// has last sent a yes
	private int candTS;
	
	// Flag indicating that this node has tried to inquire its yes inquired
	private boolean inquired;

	// Priority queue of request messages ordered by time stamp such that
	// removing the message with the lowest time stamp is an easy operation
	private PriorityQueue<RequestMessage> deferredQ;

	// District (set of node IDs) associated with this node
	private HashSet<Integer> district;
	
	// Probability that a node will delay the delivery of a message
	private double pDelay;
	
	// Probability that a node will request access to the CS
	private double pRequest;
	
	// Log for node
	Logging log = Logging.getLogger("sanders_log");
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			// Nothing
		}
	}

	@Override
	public void preStep() {
		// Nothing
	}

	@Override
	public void init() {
		inCS = false;
		currTS = 0;
		myTS = 0;
		yesVotes = 0;
		hasVoted = false;
		cand = null;
		candTS = 0;
		inquired = false;
		deferredQ = newQueue();
		district = newDistrict();
		pDelay = getDelayProbability();
		pRequest = getRequestProbability();
	}
	
	private PriorityQueue<RequestMessage> newQueue() {
		RequestMessageComparator cmp = new RequestMessageComparator();
		return new PriorityQueue<RequestMessage>(cmp);
	}
	
	private HashSet<Integer> newDistrict() {
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
	
	private double getDelayProbability() {
		try {
			return Configuration.getDoubleParameter("sanders/pdelay");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
			return 0.0;
		}
	}

	private double getRequestProbability() {
		try {
			return Configuration.getDoubleParameter("sanders/prequest");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
			return 0.0;
		}
	}

	@Override
	public void neighborhoodChange() {
		// Nothing
	}

	@Override
	public void postStep() {
		// Nothing
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// Nothing
	}

	/**
	 * A comparator implementation for ordering request messages
	 * @author Guilherme Dantas
	 */
	public class RequestMessageComparator implements Comparator<RequestMessage> {

		/**
		 * If this method returns a negative value, it means that msg1 has a
		 * higher priority over msg2, and vice-versa. If it returns zero, it
		 * means they have the same priority (which should never happen in our
		 * case since it would mean that both messages have the same sender and
		 * time stamp).
		 */
		public int compare(RequestMessage msg1, RequestMessage msg2) {
			if (msg1.getTimeStamp() == msg2.getTimeStamp()) {
				return msg1.getSender().ID - msg2.getSender().ID;
			} else {
				return msg1.getTimeStamp() - msg2.getTimeStamp();
			}
		}

	}
}
