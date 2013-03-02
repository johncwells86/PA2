package pa2;

import java.util.ArrayList;
import java.util.Queue;

public class StudentNetworkSimulator extends NetworkSimulator {
	/*
	 * Predefined Constants (static member variables):
	 * 
	 * int MAXDATASIZE : the maximum size of the Message data and Packet payload
	 * 
	 * int A : a predefined integer that represents entity A int B : a
	 * predefined integer that represents entity B
	 * 
	 * 
	 * Predefined Member Methods:
	 * 
	 * void stopTimer(int entity): Stops the timer running at "entity" [A or B]
	 * void startTimer(int entity, double increment): Starts a timer running at
	 * "entity" [A or B], which will expire in "increment" time units, causing
	 * the interrupt handler to be called. You should only call this with A.
	 * void toLayer3(int callingEntity, Packet p) Puts the packet "p" into the
	 * network from "callingEntity" [A or B] void toLayer5(int entity, String
	 * dataSent) Passes "dataSent" up to layer 5 from "entity" [A or B] double
	 * getTime() Returns the current time in the simulator. Might be useful for
	 * debugging. void printEventList() Prints the current event list to stdout.
	 * Might be useful for debugging, but probably not.
	 * 
	 * 
	 * Predefined Classes:
	 * 
	 * Message: Used to encapsulate a message coming from layer 5 Constructor:
	 * Message(String inputData): creates a new Message containing "inputData"
	 * Methods: boolean setData(String inputData): sets an existing Message's
	 * data to "inputData" returns true on success, false otherwise String
	 * getData(): returns the data contained in the message Packet: Used to
	 * encapsulate a packet Constructors: Packet (Packet p): creates a new
	 * Packet that is a copy of "p" Packet (int seq, int ack, int check, String
	 * newPayload) creates a new Packet with a sequence field of "seq", an ack
	 * field of "ack", a checksum field of "check", and a payload of
	 * "newPayload" Packet (int seq, int ack, int check) chreate a new Packet
	 * with a sequence field of "seq", an ack field of "ack", a checksum field
	 * of "check", and an empty payload Methods: boolean setSeqnum(int n) sets
	 * the Packet's sequence field to "n" returns true on success, false
	 * otherwise boolean setAcknum(int n) sets the Packet's ack field to "n"
	 * returns true on success, false otherwise boolean setChecksum(int n) sets
	 * the Packet's checksum to "n" returns true on success, false otherwise
	 * boolean setPayload(String newPayload) sets the Packet's payload to
	 * "newPayload" returns true on success, false otherwise int getSeqnum()
	 * returns the contents of the Packet's sequence field int getAcknum()
	 * returns the contents of the Packet's ack field int getChecksum() returns
	 * the checksum of the Packet int getPayload() returns the Packet's payload
	 */

	// Add any necessary class variables here. Remember, you cannot use
	// these variables to send messages error free! They can only hold
	// state information for A or B.
	// Also add any necessary methods (e.g. checksum of a String)
	protected ArrayList<Packet> aQueue;
	protected ArrayList<Packet> bQueue;
	protected int aAckCounter;
	protected int bAckCounter;
	protected int aSequence;
	protected int bSequence;
	protected double timerInterval = 5;

	protected void ResetTimerInterval() {
		timerInterval = 5.0;
	}

	protected void IncrementTimerInterval() {
		timerInterval += 5.0;
	}

	protected int AckCounter(int host) {
		if (host == A) {
			return aAckCounter++ % 2;
		} else if (host == B) {
			return bAckCounter++ % 2;
		} else {
			return -1;
		}
	}

	protected int SequenceCounter(int host) {
		if (host == A) {
			return aSequence++ % 2;
		} else if (host == B) {
			return bSequence++ % 2;
		} else {
			return -1;
		}
	}

	protected int setStringCheckSum(int host, int seq, int ack, String payload) {
		int val = 0;
		val = seq + ack;
		for (char c : payload.toCharArray()) {
			val += Character.getNumericValue(c);
		}
		return val;
	}

	protected boolean verifyChecksum(Packet p) {
		int val = 0;
		int chk = p.getChecksum();
		int ack = p.getAcknum();
		int seq = p.getSeqnum();
		val = ack + seq;
		String payload = p.getPayload();
		for (char c : payload.toCharArray()) {
			val += Character.getNumericValue(c);
		}
		return val == (chk);
	}

	// This is the constructor. Don't touch!
	public StudentNetworkSimulator(int numMessages, double loss, double corrupt, double avgDelay, int trace, long seed) {
		super(numMessages, loss, corrupt, avgDelay, trace, seed);
	}

	// This routine will be called whenever the upper layer at the sender [A]
	// has a message to send. It is the job of your protocol to insure that
	// the data in such a message is delivered in-order, and correctly, to
	// the receiving upper layer.
	protected void aOutput(Message message) {
		// receive message, make packet, set sequence, set ack, set checksum,
		// start timer.
		String data = message.getData();
		int ack = AckCounter(A);
		int seq = SequenceCounter(A);
		int chksum = setStringCheckSum(A, seq, ack, data);
//		System.out.println("aOutput called, sending message to B (to layer 3).");
		//System.out.println(String.format("Data:\t%s\nSeq:\t%d\nChk:\t%d\nAck\t%d\n", data, seq, chksum, ack));
		Packet p = new Packet(seq, ack, chksum, data);
		aQueue.add(p);

		toLayer3(A, p);
//		ResetTimerInterval();
//		startTimer(A, timerInterval);
	}

	// This routine will be called whenever a packet sent from the B-side
	// (i.e. as a result of a toLayer3() being done by a B-side procedure)
	// arrives at the A-side. "packet" is the (possibly corrupted) packet
	// sent from the B-side.
	protected void aInput(Packet packet) {
//		System.out.println("aInput called, sending message Up to A (layer 5).");
		int chk = packet.getChecksum();
		int ack = packet.getAcknum();
		// stopTimer(A);

	}

	// This routine will be called when A's timer expires (thus generating a
	// timer interrupt). You'll probably want to use this routine to control
	// the retransmission of packets. See startTimer() and stopTimer(), above,
	// for how the timer is started and stopped.
	protected void aTimerInterrupt() {
		// Lost the packet, increment the timer interval.
		System.out.println("Timer interrupt on A detected...");
		// IncrementTimerInterval();
		// // Get current sequence, current ack, send new packet...
		//
		// Packet p = aQueue.get(0);
		//
		// // Re-send the packet.
		// toLayer3(A, p);
		// startTimer(A, timerInterval);
	}

	// This routine will be called once, before any of your other A-side
	// routines are called. It can be used to do any required
	// initialization (e.g. of member variables you add to control the state
	// of entity A).
	protected void aInit() {
		this.aQueue = new ArrayList<Packet>();
		stopTimer(A);
	}

	// This routine will be called whenever a packet sent from the B-side
	// (i.e. as a result of a toLayer3() being done by an A-side procedure)
	// arrives at the B-side. "packet" is the (possibly corrupted) packet
	// sent from the A-side.
	protected void bInput(Packet packet) {
		int ack, chk, seq;
		
		String payload;
//		System.out.println("Packet from A received.");
//		System.out.println(String.format("Data:\t%s\nSeq:\t%d\nChk:\t%d\nAck\t%d\n", packet.getPayload(), packet.getSeqnum(), packet.getChecksum(), packet.getAcknum()));

		if (verifyChecksum(packet)) {
			System.out.println("Checksum verified accurate. Sending to host");
			payload = packet.getPayload();
			toLayer5(B, payload);
		}
		else{
			System.out.println("Bad Checksum... Requesting retransmit");
			packet.setPayload("");
			toLayer3(A, packet);			
		}
	}

	// This routine will be called once, before any of your other B-side
	// routines are called. It can be used to do any required
	// initialization (e.g. of member variables you add to control the state
	// of entity B).
	protected void bInit() {
		this.bAckCounter = 0;
		this.bSequence = 0;
		this.bQueue = new ArrayList<Packet>();
	}
}
