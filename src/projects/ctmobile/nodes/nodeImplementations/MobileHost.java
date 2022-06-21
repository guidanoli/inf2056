package projects.ctmobile.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
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

	// all mobile host nodes
	private static Vector<MobileHost> allMHs = new Vector<MobileHost>();
	
	private static Random random = Distribution.getRandom();
	private static int maxValue;
	private static double pStart;

	{
		try {
			maxValue = Configuration.getIntegerParameter("ctmobile/MobileHost/maxValue");
			pStart = Configuration.getDoubleParameter("ctmobile/MobileHost/pStart");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
		}
	}
	
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
	
	// Value provided by the application program running on a mobile host
	private int initialValue;
	
	private Logging logger = Logging.getLogger("ctmobile.log");
	
	@Override
	public void handleMessages(Inbox inbox) {
		for (Message msg : inbox) {
			logger.logln(LogL.MH, this + " received " + msg);
			if (msg instanceof Init3) {
				// Action 2
				Node sender = inbox.getSender();
				loggedSend(new Propose(this, initialValue), sender);
			} else if (msg instanceof Decide) {
				// Action 3
				appState = ApplicationState.ReachedConsensus;
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
			appState = ApplicationState.RequestingConsensus;
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
			color = Color.WHITE;
			break;
		}
		this.setColor(color);
		String text = Integer.toString(getIndex());
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 24, Color.BLACK);
	}
	
	@Override
	public void init() {
		allMHs.add(this);
		initialValue = random.nextInt(maxValue);
		appState = ApplicationState.Idle;
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
		if (someMSS != null) {
			// Hand-off procedure (1)
			loggedSend(new Guest(this, mss), someMSS);
			mss = someMSS;
		}
	}
	
	@Override
	public void postStep() {
		if (appState == ApplicationState.RequestingConsensus && mss != null) {
			// Action 1
			loggedSend(new Init1(), mss);
			appState = ApplicationState.AwaitingConsensus;
		}
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
