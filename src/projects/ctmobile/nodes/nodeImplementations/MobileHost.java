package projects.ctmobile.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import projects.ctmobile.LogL;
import projects.ctmobile.nodes.messages.Decide;
import projects.ctmobile.nodes.messages.Guest;
import projects.ctmobile.nodes.messages.Init1;
import projects.ctmobile.nodes.messages.Init3;
import projects.ctmobile.nodes.messages.Propose;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Main;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;

public class MobileHost extends Node {

	/**
	 * Static variables
	 */
	
	// all mobile host nodes
	public static Vector<MobileHost> allMHs = new Vector<MobileHost>();
	
	// random number generator from the framework
	private static Random random = Distribution.getRandom();
	
	// maximum value allowed to be generated by application
	private static int maxValue;
	
	// probability of a MH requesting consensus
	private static double pStart;

	{
		try {
			maxValue = Configuration.getIntegerParameter("ctmobile/MobileHost/maxValue");
			pStart = Configuration.getDoubleParameter("ctmobile/MobileHost/pStart");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
		}
	}
	
	/**
	 * Instance variables
	 */
	
	enum ApplicationState {
		Idle,
		RequestingConsensus,
		AwaitingConsensus,
		ReachedConsensus,
	}
	
	// The state of the application that consumes the consensus middleware
	private ApplicationState appState;
	
	// The mobile support station with whom this node communicates directly
	private MobileSupportStation mss;
	
	// The mobile support station with whom this node last communicated with
	private MobileSupportStation lastMSS;
		
	// Value provided by the application program running on a mobile host
	public int initialValue = -1;
	
	// Value chosen by the distributed consensus algorithm
	public HashSet<Integer> consensus;
	
	// Logger object
	private Logging logger = Logging.getLogger("ctmobile.log");
	
	public static int getNumberOfInstances() {
		return allMHs.size();
	}
	
	public void loggedAppStateChange(ApplicationState newState) {
		logger.logln(LogL.MH_APP_STATES, this + " changed its app state: " + appState + " -> " + newState);
		appState = newState;
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		for (Message msg : inbox) {
			logger.logln(LogL.MH, this + " received " + msg);
			if (msg instanceof Init3) {
				// Action 2
				loggedSend(new Propose(this, initialValue), inbox.getSender());
			} else if (msg instanceof Decide) {
				// Action 3
				loggedAppStateChange(ApplicationState.ReachedConsensus);
				consensus = ((Decide)msg).v;
			}
		}
	}

	public void loggedSend(Message m, Node target) {
		send(m, target);
		logger.logln(LogL.MH, this + " sent " + m + " to " + target);
	}
	
	public int getIndex() {
		return allMHs.indexOf(this) + 1;
	}
	
	@Override
	public String toString() {
		return "MH_" + getIndex();
	}
	
	@Override
	public void preStep() {
		// application may randomly request consensus to start
		if (appState == ApplicationState.Idle && random.nextDouble() < pStart) {
			loggedAppStateChange(ApplicationState.RequestingConsensus);
		}
	}

	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		Color color;
		switch (appState) {
		case RequestingConsensus:
			color = Color.YELLOW;
			break;
		case AwaitingConsensus:
			color = Color.ORANGE;
			break;
		case ReachedConsensus:
			color = Color.GREEN;
			break;
		default:
			color = Color.GRAY;
			break;
		}
		this.setColor(color);
		super.drawNodeAsDiskWithText(g, pt, highlight, "", 24, Color.BLACK);
	}
	
	@Override
	public void init() {
		allMHs.add(this);
		appState = ApplicationState.Idle;
		initialValue = random.nextInt(maxValue);
		consensus = null;
	}

	@Override
	public void neighborhoodChange() {
		MobileSupportStation someMSS = null;
		for (Edge e : this.outgoingConnections) {
			Node node = e.endNode;
			if (node instanceof MobileSupportStation) {
				someMSS = (MobileSupportStation)node;
				assert(someMSS != null);
				if (mss == someMSS) {
					// the MH is still connected to its MSS
					return;
				}
			}
		}
		// the MH is not connected to a MSS
		// So we check if there is some other MSS
		// that is different from the last MSS
		if (someMSS != null && someMSS != lastMSS) {
			// Hand-off procedure (1)
			loggedSend(new Guest(this, lastMSS), someMSS);
			logger.logln(LogL.HANDOFF, this + " now talks to " + someMSS);
		}
		// Update MSS (might be null)
		mss = someMSS;
		// Update Last MSS (not null)
		if (mss != null) {
			lastMSS = mss;
		}
	}
	
	@Override
	public void postStep() {
		// Action 1
		if ((appState == ApplicationState.RequestingConsensus ||
				appState == ApplicationState.AwaitingConsensus) && mss != null) {
			loggedSend(new Init1(), mss);
			loggedAppStateChange(ApplicationState.AwaitingConsensus);
		}
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
