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

import java.util.HashSet;
import java.util.PriorityQueue;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

/**
 * Node that implements the distributed mutual exclusion algorithm specified on (Sanders, 1987).
 * @author Guilherme Dantas
 */
public class SandersNode extends Node {

	// District (set of nodes) associated with this node
	private HashSet<SandersNode> district;
	
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
	// a request message from it
	private SandersNode cand;
	
	// Time stamp of the request message sent by the node to whom this node
	// has last sent a yes
	private int candTS;
	
	// Flag indicating that this node has tried to inquire its yes inquired
	private boolean inquired;
	
	// Priority queue of request messages ordered by time stamp such that
	// removing the message with the lowest time stamp is an easy operation
	// TODO: Make it a priority queue of RequestMessage
	private PriorityQueue<SandersNode> deferredQ;
	
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
		// Nothing
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

}
