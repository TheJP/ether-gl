package ch.fhnw.ether.examples.video.fx;

import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.ether.video.fx.IVideoGLFX;

public class RadialBlur extends AbstractVideoFX implements IVideoGLFX {
	static final Parameter DEPTH = new Parameter("depth", "depth", 0, 50, 0);
	
	public RadialBlur() {
		super(DEPTH);
	}
	
	@Override
	public String mainFrag() {
		return lines(
				"if(depth < 1) return;",
				"ivec2 size       = textureSize(frame,0);",
				"vec3  resolution = vec3(size.x, size.y,1.);",
				"vec3  p          = gl_FragCoord.xyz/resolution-.5;",
				"vec3  o          = texture(frame,.5+(p.xy*=.992)).rbb;",
				"for (float i=0.;i<depth;i++)", 
				"    p.z += pow(max(0.,.5-length(texture(frame,.5+(p.xy*=.992)).rg)),2.)*exp(-i*.08);",
				"result = vec4(o*o+p.z,1);"
				);
	};
}
