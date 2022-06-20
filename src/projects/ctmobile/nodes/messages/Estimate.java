package projects.ctmobile.nodes.messages;

import java.util.HashSet;

import sinalgo.nodes.messages.Message;

/**
 * This message carries the estimate proposed by a base station MSS_i to the current coordinator MSS_c.
 * Each estimate is tagged with a time stamp ts_i identifying the round during which MSS_i has updated
 * its estimate for the last time (see action 12). During round r, MSS_i sends a first ESTIMATE message
 * during action 10. Other ESTIMATE messages can be sent during action 5 when MSS_i updates its collection
 * of values. The estimates sent during round r to MSS_c (MSS_c is necessarily the coordinator of round r),
 * are gathered and logged in a local buffer Log_c[r]. A base station MSSi can propose multiple estimates
 * during a round r but the coordinator MSS_c keeps only the most recent estimate sent by MSS_i during round r.
 * The statement Log_c[r] := Log_c[r] @ (MSS_i,r, V_i,ts_i) executed in action 7 is equivalent to two
 * successive operations: (1) Log_c[r] := Log_c[r] U { (MSS_i,r,V_i',ts_i)} and (2) if there exists
 * (MSS_i,r,V_i',ts_i) E Log_c[r] and (MSS_i,r,V_i'',ts_i) E Log_c[r] such that card(V_i') <= card(V_i"),
 * then (MSS_i,r,V_i', ts_i) is removed from Log_c[r].
 */
public class Estimate<T> extends Message {

	public int MSS_j;
	public int r;
	public HashSet<T> V_j;
	public HashSet<Integer> P_j;
	public int ts_j;
	
	/**
	 * 
	 * @param MSS_j id of base station proposing estimate
	 * @param r round in which MSS_j is in
	 * @param V_j set of values
	 * @param P_j set of mobile workers that have proposed
	 * @param ts_j time stamp of estimation
	 */
	@SuppressWarnings("unchecked")
	public Estimate(int MSS_j, int r, HashSet<T> V_j, HashSet<Integer> P_j, int ts_j) {
		this.MSS_j = MSS_j;
		this.r = r;
		this.V_j = (HashSet<T>)V_j.clone();
		this.P_j = (HashSet<Integer>)P_j.clone();
		this.ts_j = ts_j;
	}
	
	@Override
	public Message clone() {
		return new Estimate<T>(MSS_j, r, V_j, P_j, ts_j);
	}

}
