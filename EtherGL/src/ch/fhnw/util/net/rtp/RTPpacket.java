package ch.fhnw.util.net.rtp;

public class RTPpacket extends AbstractRTPpacket {
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
	
	@Override
	public String toString() {
		String result = "[RTP]";
		result += s(0, 4, " V=",     0, 2);
		result += s(0, 4, " P",      2, 1);
		result += s(0, 4, " X",      3, 1);
		result += s(0, 4, " CC=",    4, 4);
		result += s(0, 4, " M",      8, 1);
		result += s(0, 4, " PT=",    9, 7);
		result += s(0, 4, " seqNb=",16,16);
		result += s(4, 4, " ts=",    0,32);
		result += s(8, 4, " ssrc=",  0,32);
		if(bitx(0,4,9,7) == MJPEG_TYPE) {
			result += s(12, 4, " type_spec=", 0, 8);
			result += s(12, 4, " frag_off=",  8, 24);
			result += s(16, 4, " type=",      0, 8);
			result += s(16, 4, " Q=",         8, 8);
			result += s(16, 4, " Width=",    16, 8);
			result += s(16, 4, " Height=",   24, 8);
		}
		return result;
	}
}
