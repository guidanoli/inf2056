package projects.ctmobile.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

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
import sinalgo.tools.statistics.Distribution;

public class MobileHost extends Node {

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
	
	@Override
	public void handleMessages(Inbox inbox) {
		for (Message msg : inbox) {
			Node sender = inbox.getSender();
			if (msg instanceof Init3) {
				// Action 2
				send(new Propose(ID, initialValue), sender);
			} else if (msg instanceof Decide) {
				// Action 3
				appState = ApplicationState.ReachedConsensus;
			}
		}
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
		String text = Integer.toString(initialValue);
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 24, Color.BLACK);
	}
	
	@Override
	public void init() {
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
				if (mss != null && mss == someMSS) {
					break; // still connected to current MSS
				}
			}
		}
		if (mss != null && someMSS != null && mss != someMSS) {
			// Hand-off procedure (1)
			send(new Guest(this, mss), someMSS);
		}
		// update MSS
		mss = someMSS;
	}

	@Override
	public void postStep() {
		if (appState == ApplicationState.RequestingConsensus && mss != null) {
			// Action 1
			send(new Init1(), mss);
			appState = ApplicationState.AwaitingConsensus;
		}
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
