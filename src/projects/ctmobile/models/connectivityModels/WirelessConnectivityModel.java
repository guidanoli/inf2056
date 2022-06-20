package projects.ctmobile.models.connectivityModels;

import projects.ctmobile.nodes.nodeImplementations.MobileHost;
import projects.ctmobile.nodes.nodeImplementations.MobileSupportStation;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;

/**
* Implements a connection from a mobile host to a mobile support station
*/
public class WirelessConnectivityModel extends ConnectivityModelHelper {

	private static boolean initialized = false; // indicates whether the static fields of this class have already been initialized 
	private static double rMaxSquare; // we reuse the rMax value from the GeometricNodeCollection.
	
	/**
	 * The constructor reads the configuration file.
	 * @throws CorruptConfigurationEntryException When there is a missing entry in the configuration file.
	 */
	public WirelessConnectivityModel() throws CorruptConfigurationEntryException {
		if(! initialized) { // only initialize once
			double geomNodeRMax = Configuration.getDoubleParameter("GeometricNodeCollection/rMax");
			try {
				rMaxSquare = Configuration.getDoubleParameter("UDG/rMax");
			} catch(CorruptConfigurationEntryException e) {
				Global.log.logln("\nWARNING: Did not find an entry for UDG/rMax in the XML configuration file. Using GeometricNodeCollection/rMax.\n");
				rMaxSquare = geomNodeRMax;
			}
			if(rMaxSquare > geomNodeRMax) { // dangerous! This is probably not what the user wants!
				Main.minorError("WARNING: The maximum transmission range used for the UDG connectivity model is larger than the maximum transmission range specified for the GeometricNodeCollection.\nAs a result, not all connections will be found! Either fix the problem in the project-specific configuration file or the '-overwrite' command line argument.");
			}
			rMaxSquare = rMaxSquare * rMaxSquare;
			initialized = true;
		}
	}

	protected boolean isConnected(Node from, Node to) {
		// MSSs are hard-wired amongst themselves.
		// MHs are not connected among themselves.
		// MSSs and MHs are connected when close enough.
		if(from instanceof MobileSupportStation && to instanceof MobileHost ||
				to instanceof MobileSupportStation && from instanceof MobileHost) {
			double dist = from.getPosition().squareDistanceTo(to.getPosition());
			return dist < rMaxSquare;
		}
		return false;
	}

}
