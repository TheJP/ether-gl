package ch.fhnw.ether.midi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import ch.fhnw.ether.media.AbstractFrameSource;
import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;

public class URLMidiSource extends AbstractFrameSource<IMidiRenderTarget, URLMidiSource.State> {
	private final Sequence seq;
	private final int      numPlays;
	private final URL      url;
	private final long     frameCount;

	public URLMidiSource(URL url) throws InvalidMidiDataException, IOException {
		this(url, Integer.MAX_VALUE);
	}

	public URLMidiSource(final URL url, final int numPlays) throws IOException, InvalidMidiDataException {
		this.url      = url;
		this.seq      = MidiSystem.getSequence(url);
		this.numPlays = numPlays;
		if(seq.getDivisionType() != Sequence.PPQ)
			throw new IOException("Only PPQ Sequence supported");

		Track[] tracks    = seq.getTracks();
		long    frameCount  = 0;
		int[]   trackspos = new int[tracks.length];
		long    lasttick   = -1;
		int     msgs       = 0;
		
		frameLoop:
			for(;;) {
				for(;;) {
					MidiEvent selevent = null;
					int       seltrack = -1;
					for (int i = 0; i < tracks.length; i++) {
						int trackpos = trackspos[i];
						Track track = tracks[i];
						if (trackpos < track.size()) {
							MidiEvent event = track.get(trackpos);
							if (selevent == null || event.getTick() < selevent.getTick()) {
								selevent = event;
								seltrack = i;
							}
						}
					}
					if (seltrack == -1)
						break frameLoop;

					trackspos[seltrack]++;
					long tick = selevent.getTick();
					if(lasttick < 0)
						lasttick = tick;
					boolean setFrame = lasttick != tick;
					lasttick = tick;
					MidiMessage msg = selevent.getMessage();
					if(!(msg instanceof MetaMessage))
						msgs++;
					if(setFrame && msgs > 0)
						break;
				}
				frameCount++;
				msgs = 0;
			}

		this.frameCount = frameCount;
	}

	@Override
	protected void run(State s) throws RenderCommandException {
		s.runInternal();
	}

	@Override
	protected State createState(IMidiRenderTarget target) {
		return new State(target);
	}

	class State extends PerTargetState<IMidiRenderTarget> {
		private final float             divtype;
		private final Track[]           tracks;
		private final int               seqres;
		private final int[]             trackspos;
		private       int               mpq        = (int)AbstractMediaTarget.SEC2US / 2;
		private       long              lasttick   = -1;
		private       long              curtime    = 0;
		private       int               numPlays;
		private       double            startTime  = -1;
		private final List<MidiMessage> msgs = new ArrayList<>();
		int count;

		State(IMidiRenderTarget target) {
			super(target);
			this.divtype   = seq.getDivisionType();
			this.tracks    = seq.getTracks();
			this.seqres    = seq.getResolution();
			this.trackspos = new int[tracks.length];
			this.numPlays  = URLMidiSource.this.numPlays;
		}

		void runInternal() {
			if(numPlays <= 0) return;

			if(startTime < 0)
				startTime = getTarget().getTime();

			for(;;) {
				MidiEvent selevent = null;
				int       seltrack = -1;
				for (int i = 0; i < tracks.length; i++) {
					int trackpos = trackspos[i];
					Track track = tracks[i];
					if (trackpos < track.size()) {
						MidiEvent event = track.get(trackpos);
						if (selevent == null || event.getTick() < selevent.getTick()) {
							selevent = event;
							seltrack = i;
						}
					}
				}
				if (seltrack == -1) {
					Arrays.fill(trackspos, 0);
					numPlays--;
					startTime = -1;
					count = 0;
					return;
				}
				trackspos[seltrack]++;
				long tick = selevent.getTick();
				if(lasttick < 0)
					lasttick = tick;
				if (divtype == Sequence.PPQ)
					curtime += ((tick - lasttick) * mpq) / seqres;
				else
					curtime = tick;
				boolean setFrame = lasttick != tick;
				lasttick = tick;
				MidiMessage msg = selevent.getMessage();
				if (msg instanceof MetaMessage) {
					if (divtype == Sequence.PPQ)
						if (((MetaMessage) msg).getType() == 0x51) {
							byte[] data = ((MetaMessage) msg).getData();
							mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
						}
				} else
					msgs.add(msg);
				if(setFrame && !msgs.isEmpty())
					break;
			}
			getTarget().setFrame(new MidiFrame(startTime + (curtime / AbstractMediaTarget.SEC2US), msgs.toArray(new MidiMessage[msgs.size()])));
			msgs.clear();
		}
	}

	@Override
	public String toString() {
		return url.toString();
	}

	@Override
	public long getFrameCount() {
		return frameCount;
	}
}