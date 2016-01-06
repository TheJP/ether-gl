package ch.fhnw.util.net.rtp;

import ch.fhnw.util.ByteList;

public class RTPpacket {
	final static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video

	/*
	   0                   1                   2                   3
	   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |V=2|P|X|  CC   |M|     PT      |       sequence number         |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                           timestamp                           |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |           synchronization source (SSRC) identifier            |
	   +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
	   |            contributing source (CSRC) identifiers             |
	   |                             ....                              |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */

	private ByteList packet = new ByteList();
	
	private void add(int v) {
		packet.add((byte)v);
	}
	
	public RTPpacket(int payloadType, int seqNb, int timestamp) {
		add(0x80);
		add(payloadType & 0x7F);
		add(seqNb >> 8);
		add(seqNb);
		add(timestamp >> 24);
		add(timestamp >> 16);
		add(timestamp >> 8);
		add(timestamp);
		int ssrc = 1601061148;
		add(ssrc >> 24);
		add(ssrc >> 16);
		add(ssrc >> 8);
		add(ssrc);
	}

	public void set_marker(boolean state) {
		if(state)
			packet._getArray()[1] |= 0x80;
		else
			packet._getArray()[1] &= ~0x80;
	}

	public ByteList getPayload() {
		return packet;
	}

	public byte[] getPacket() {
		return packet.toArray();
	}

	public int size() {
		return packet.size();
	}
}
