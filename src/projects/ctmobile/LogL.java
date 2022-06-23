/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.ctmobile;

/**
 * Enumerates the log-levels. Levels above THRESHOLD will be included
 * in the log-file. The levels below (with a higher enumeration value) not.
 */
public class LogL extends sinalgo.tools.logging.LogL{
	
	// Mobile host message sending and receiving
	public static final boolean MH = false;
	
	// Mobile host application state changes
	public static final boolean MH_APP_STATES = false;

	// Mobile support station message sending and receiving
	public static final boolean MSS = false;

	// Mobile support station phase changes
	public static final boolean MSS_PHASES = false;

	// Mobile support station changes related to the P set
	public static final boolean MSS_P_SET = false;

	// Mobile support station changes related to the V / New_V sets
	public static final boolean MSS_V_SET = false;

	// Mobile support station changes related to EndCollect
	public static final boolean MSS_END_COLLECT = false;

	// Mobile support station changes related to r
	public static final boolean MSS_ROUND = false;

	// Mobile support station changes related to the log of estimates
	public static final boolean MSS_LOG = false;

	// Mobile support station changes related to acknowledgement
	public static final boolean MSS_ACKS = false;
	
	// Mobile support station changes related to the state
	public static final boolean MSS_STATE = false;
	
	// Mobile support station changes related to the time stamp
	public static final boolean MSS_TS = false;
	
	// Hand-off procedure details
	public static final boolean HANDOFF = false;
}


