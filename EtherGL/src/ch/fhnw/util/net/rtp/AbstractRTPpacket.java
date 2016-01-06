package ch.fhnw.util.net.rtp;

import ch.fhnw.util.ByteList;
import ch.fhnw.util.ClassUtilities;

public class AbstractRTPpacket {
	protected ByteList packet = new ByteList();

	public ByteList getPayload() {
		return packet;
	}

	public byte[] getPacket() {
		return packet.toArray();
	}

	public int size() {
		return packet.size();
	}

	private static final int[] MASKS = new int[33];
	static {
		int val = 0;
		for(int i = 0; i < 33; i++) {
			MASKS[i] = val;
			val |= (1 << i);
		}
	}

	protected int bitx(int off, int len, int low, int blen) {
		int result = 0;
		low = 31-(low+blen);
		while(--len >= 0) {
			result <<= 8;
			result |= packet.get(off++) & 0xFF;
		}
		result >>>= low+1;
			result &= MASKS[blen];
			return result;
	}
	
	protected String s(int off, int len, String txt, int low, int blen) {
		int result = bitx(off, len, low, blen);
		if(blen > 1)
			return txt + result;
		return result != 0 ? txt : ClassUtilities.EMPTY_String;
	}
	
	protected void add(int v) {
		packet.add((byte)v);
	}
}
