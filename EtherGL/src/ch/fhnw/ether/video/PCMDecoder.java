package ch.fhnw.ether.video;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat.Encoding;

import org.jcodec.common.AudioDecoder;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.AudioBuffer;

public class PCMDecoder implements AudioDecoder {
	private final AudioFormat format;

	public PCMDecoder(javax.sound.sampled.AudioFormat format) {
		this.format = new AudioFormat(
				(int)format.getSampleRate(),
				format.getSampleSizeInBits(),
				format.getChannels(), 
				format.getEncoding() == Encoding.PCM_SIGNED, 
				format.isBigEndian());
	}

	@Override
	public AudioBuffer decodeFrame(ByteBuffer frame, ByteBuffer dst) {
		ByteBuffer dup = dst.duplicate();
		NIOUtils.write(dst, frame);
		dup.flip();
		return new AudioBuffer(dup, format, dup.remaining() / format.getFrameSize());
	}
}
