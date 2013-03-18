package pa2;

import java.util.ArrayList;
import java.util.Queue;

public class StudentNetworkSimulatorAB extends NetworkSimulator {
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
	protected Packet bLastReceivedFromA;
	protected Packet aLastReceivedFromB;

	protected int aAckCounter = 0;
	protected int bAckCounter = 0;
	protected int aSequence = 0;
	protected int bSequence = 0;
	protected int totalGood = 0;
	protected double timerInterval = 1000.0;
	protected boolean isSending;
	protected boolean isTiming;

	protected void IncrementTimerInterval() {
		timerInterval += 500.0;
	}

	protected int setStringCheckSum(int host, int seq, int ack, String payload) {
		int val = 0;
		val = seq + ack;

		// Only A packets have payloads to evaluate.
		if (host == A) {
			for (char c : payload.toCharArray()) {
				val += Character.getNumericValue(c);
			}
		}
		return val;
	}

	protected boolean verifyChecksum(int host, Packet p) {
		int val = 0;
		int chk = p.getChecksum();
		int ack = p.getAcknum();
		int seq = p.getSeqnum();
		val = ack + seq;

		// Only check payload if host is B, because A only receives acks.
		if (host == B) {
			String payload = p.getPayload();
			for (char c : payload.toCharArray()) {
				val += Character.getNumericValue(c);
			}
		}
		return val == (chk);
	}

	// This is the constructor. Don't touch!
	public StudentNetworkSimulatorAB(int numMessages, double loss, double corrupt, double avgDelay, int trace, long seed) {
		super(numMessages, loss, corrupt, avgDelay, trace, seed);
	}

	// This routine will be called whenever the upper layer at the sender [A]
	// has a message to send. It is the job of your protocol to insure that
	// the data in such a message is delivered in-order, and correctly, to
	// the receiving upper layer.
	protected void aOutput(Message message) {
		System.out.println("\n===================\naOutput:\t Sending message to B (to layer 3).");
		String data = message.getData();
		int ack = this.aSequence;
		int seq = this.aSequence;
		int chksum = setStringCheckSum(A, seq, ack, data);

		Packet p = new Packet(seq, ack, chksum, data);
		aQueue.add(p);

		// while (!aQueue.isEmpty()) {
		// if (!isSending) {
		isSending = true;

		 if (isTiming) {
		isTiming = false;
		stopTimer(A);
		 }

		startTimer(A, timerInterval);
		isTiming = true;
		toLayer3(A, aQueue.get(0));
		// } else {
		// System.out.println("Discarding");
		// }
		// }

	}

	// This routine will be called whenever a packet sent from the B-side
	// (i.e. as a result of a toLayer3() being done by a B-side procedure)
	// arrives at the A-side. "packet" is the (possibly corrupted) packet
	// sent from the B-side.
	protected void aInput(Packet packet) {
//		System.out.println("\naInput:\t Verifying packet integrity.");

		stopTimer(A);
		isTiming = false;

		int seq = this.aSequence;
		int ack = packet.getAcknum();
		System.out.println(String.format("A:\naInput:\t Seq: %s\t Packet Seq: %s\t Packet Ack: %s", seq, packet.getSeqnum(), packet.getAcknum()));
		boolean goodChk = verifyChecksum(A, packet);
		if (goodChk && (seq == ack)) {

			isSending = false;
			this.totalGood++;
			System.out.println("aInput:\t Successfully sent packet to B, total: " + this.totalGood);

			// Packet was sent to B successfully, remove from queue.

			aQueue.remove(0);

			// Change the seq counter for A.
			if (this.aSequence == 0) {
				this.aSequence = 1;
			} else {
				this.aSequence = 0;
			}
			toLayer5(A, packet.getPayload());
		} else {
			// Packet was not sent successfully, resend last to B.

			System.out.println("aInput:\t Packet sent to B corrupt. Resending.");
			startTimer(A, timerInterval);
			isTiming = true;
			toLayer3(A, aQueue.get(0));
		}
		// }
	}

	// This routine will be called when A's timer expires (thus generating a
	// timer interrupt). You'll probably want to use this routine to control
	// the retransmission of packets. See startTimer() and stopTimer(), above,
	// for how the timer is started and stopped.
	protected void aTimerInterrupt() {
		// Lost the packet, increment the timer interval.
		System.out.println("aTimerInterrupt:\t Timer interrupt on A detected...");
		// System.out.println(timerInterval);
		isTiming = false;
		// IncrementTimerInterval();

		// Get current sequence, current ack, send new packet...
		if (aQueue.size() > 0) {
			Packet p = aQueue.get(0);

			startTimer(A, timerInterval);
			isTiming = true;
			toLayer3(A, p);
		}

	}

	// This routine will be called once, before any of your other A-side
	// routines are called. It can be used to do any required
	// initialization (e.g. of member variables you add to control the state
	// of entity A).
	protected void aInit() {
		this.aSequence = 0;
		this.aLastReceivedFromB = null;
		this.isSending = false;
		this.isTiming = false;
		this.aQueue = new ArrayList<Packet>();
	}

	// This routine will be called whenever a packet sent from the B-side
	// (i.e. as a result of a toLayer3() being done by an A-side procedure)
	// arrives at the B-side. "packet" is the (possibly corrupted) packet
	// sent from the A-side.
	protected void bInput(Packet packet) {
		System.out.println("\nbInput:\t Packet from A received.\tPayload: " + packet.getPayload());
		if (packet.getSeqnum() != this.bSequence) {
			// Duplicate packet.
			System.out.println("bInput:\t Duplicate Packet from A received.");
			System.out.println(String.format("bInput:\t Seq: %s\t Packet Seq: %s\tPacket Ack: %s", bSequence, packet.getSeqnum(), packet.getAcknum()));
			packet.setPayload(" ");
			int seq = packet.getSeqnum();
			toLayer3(B, new Packet(seq, seq, setStringCheckSum(B, seq, seq, " "), " "));
		} else {
			// System.out.println("bInput:\t Unique Packet from A received.");
			this.bLastReceivedFromA = packet;
			int seq = this.bSequence;
			int chk = setStringCheckSum(B, seq, seq, " ");
			String payload;
			System.out.println(String.format("bInput:\t Seq: %s\t Packet Seq: %s\t", seq, packet.getSeqnum()));

			if (verifyChecksum(B, packet) && (seq == packet.getSeqnum())) {
				System.out.println("bInput:\t Good Packet from A received.");
				// System.out.println("bInput:\t Checksum verified accurate. Sending to host");
				toLayer3(B, new Packet(seq, seq, chk, " ")); // Send ack.
				System.out.println(String.format("bInput:\t Seq: %s\t Packet Seq: %s\tPacket Ack: %s", bSequence, packet.getSeqnum(), packet.getAcknum()));
				// Increment sequence number
				if (this.bSequence == 0) {
					this.bSequence = 1;
				} else {
					this.bSequence = 0;
				}

				// Send to B's host
				payload = packet.getPayload();
				toLayer5(B, payload);

			} else {
				System.out.println("bInput:\t Bad Checksum... Requesting retransmit");
				// System.out.println(String.format("==========Received===========\nAck: %d\t Chk: %d\t Seq: %d\t Payload: %s\t",
				// packet.getAcknum(), packet.getChecksum(), packet.getSeqnum(),
				// packet.getPayload()));
				// System.out.println(String.format("==========Expected===========\nAck: %d\t Chk: %d\t Seq: %d\t Payload: %s\t\n\n",
				// seq, chk, seq, packet.getPayload()));
				packet.setPayload(" ");
				toLayer3(B, new Packet(packet));
			}
		}
	}

	// This routine will be called once, before any of your other B-side
	// routines are called. It can be used to do any required
	// initialization (e.g. of member variables you add to control the state
	// of entity B).
	protected void bInit() {
		this.bLastReceivedFromA = null;
		this.bAckCounter = 0;
		this.bSequence = 0;
		this.bQueue = new ArrayList<Packet>();
	}
}
