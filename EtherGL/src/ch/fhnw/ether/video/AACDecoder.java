package ch.fhnw.ether.video;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat.Encoding;

import org.jcodec.common.AudioDecoder;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.model.AudioBuffer;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.Profile;
import net.sourceforge.jaad.aac.SampleBuffer;

public class AACDecoder implements AudioDecoder {
	private final AudioFormat format;
	private final Decoder     decoder;

	public AACDecoder(byte[] decoderSpecificInfo, javax.sound.sampled.AudioFormat format) throws AACException {
		this.format = new AudioFormat(
				(int)format.getSampleRate(),
				format.getSampleSizeInBits(),
				format.getChannels(), 
				format.getEncoding() == Encoding.PCM_SIGNED, 
				format.isBigEndian());
		this.decoder = new Decoder(decoderSpecificInfo);
	}

	public static boolean canDecode(Profile profile) {
		return Decoder.canDecode(profile);
	}

	@Override
	public AudioBuffer decodeFrame(ByteBuffer frame, ByteBuffer dst) throws IOException {
		try {
			SampleBuffer buffer = new SampleBuffer();
			decoder.decodeFrame(frame.array(), buffer);
			byte[] data = buffer.getData();
			return new AudioBuffer(ByteBuffer.wrap(data), format, data.length / format.getFrameSize());
		} catch(Throwable t) {
			throw new IOException(t);
		}
	}
}
