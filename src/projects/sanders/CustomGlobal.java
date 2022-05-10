package projects.sanders;

import java.util.Enumeration;

import projects.sanders.nodes.nodeImplementations.SandersNode;
import projects.sanders.nodes.nodeImplementations.SandersNode.State;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}
	
	/**
	 * Check if there is at most one node in the critical section
	 */
	@Override
	public void postRound() {
		Enumeration<Node> nodeEnumer = Tools.getNodeList().getNodeEnumeration();
		boolean hasNodeInCS = false;
		while(nodeEnumer.hasMoreElements()){
			Node node = nodeEnumer.nextElement();
			if (node instanceof SandersNode) {
				SandersNode sandersNode = (SandersNode) node;
				if (sandersNode.getState() == State.InCS) {
					if (hasNodeInCS) {
						Tools.fatalError("There are two nodes in the critical section");
					} else {
						hasNodeInCS = true;
					}
				}
			}
		}
	}
}
