package ch.fhnw.util.net.rtp;


//RR: Receiver Report RTCP Packet
/*
        0                   1                   2                   3  
        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
header |V=2|P|    RC   |   PT=RR=201   |             length            |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                     SSRC of packet sender                     |
       +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
report |                 SSRC_1 (SSRC of first source)                 |
block  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  1    | fraction lost |       cumulative number of packets lost       |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |           extended highest sequence number received           |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                      interarrival jitter                      |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                         last SR (LSR)                         |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                   delay since last SR (DLSR)                  |
       +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
report |                 SSRC_2 (SSRC of second source)                |
block  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
2      :                               ...                             :
       +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
       |                  profile-specific extensions                  |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

class RTCPpacket extends AbstractRTPpacket {
	// Constructor from bit stream
	public RTCPpacket(byte[] packet, int packet_size) {
		this.packet.addAll(packet, 0, packet_size);
	}

	@Override
	public String toString() {
		if(size() < 8) return "Unknown packet (size = " + size() + ")";

		String result = "[RTCP]";
		result += s(0, 4, " V=",      0, 2);
		result += s(0, 4, " P",       2, 1);
		result += s(0, 4, " RC=",     3, 5);
		result += s(0, 4, " PT=",     8, 8);
		result += s(0, 4, " length=",16,16);
		result += s(4, 4, " ssrc=",   0,32);
		int off = 8;
		int pt  = bitx(0, 4, 8, 8);
		switch(pt) {
		case 201:
			for(int rc = bitx(0, 4, 3, 5); -- rc >= 0;) {
				result += s(off,4, " ssrc=",  0,32); off += 4;
				result += s(off,4, " flost=",  0,8); 
				result += s(off,4, " clost=",  8,24); off += 4;
				result += s(off,4, " hSeqNb=", 0,32); off += 4;
				result += s(off,4, " jitter=", 0,32); off += 4;
				result += s(off,4, " lsr=",    0,32); off += 4;
				result += s(off,4, " dlsr=",   0,32); off += 4;
			}
			break;
		default:
			result += " Error:Unknown payload type:" + pt; 
		}
		return result;
	}
}