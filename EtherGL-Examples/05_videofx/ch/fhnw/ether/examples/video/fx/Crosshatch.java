package ch.fhnw.ether.examples.video.fx;

import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.ether.video.fx.IVideoGLFX;

public class Crosshatch extends AbstractVideoFX implements IVideoGLFX {
	@Override
	public String mainFrag() {
		return lines(
				"const float hatch_y_offset  = 5.0;",
				"const float lum_threshold_1 = 1.0;",
				"const float lum_threshold_2 = 0.7;",
				"const float lum_threshold_3 = 0.5;",
				"const float lum_threshold_4 = 0.3;",
				"vec3 tc = vec3(1.0, 0.0, 0.0);",
				"float lum = length(result.rgb);",
				"tc = vec3(1.0, 1.0, 1.0);",
				"if (lum < lum_threshold_1) {",
				"  if (mod(gl_FragCoord.x + gl_FragCoord.y, 10.0) == 0.0)", 
				"    tc = vec3(0.0, 0.0, 0.0);",
				"}",
				"if (lum < lum_threshold_2) {",
				"  if (mod(gl_FragCoord.x - gl_FragCoord.y, 10.0) == 0.0)", 
				"    tc = vec3(0.0, 0.0, 0.0);",
				"}",
				"if (lum < lum_threshold_3) {",
				"  if (mod(gl_FragCoord.x + gl_FragCoord.y - hatch_y_offset, 10.0) == 0.0)", 
				"    tc = vec3(0.0, 0.0, 0.0);",
				"}",
				"if (lum < lum_threshold_4) {",
				"  if (mod(gl_FragCoord.x - gl_FragCoord.y - hatch_y_offset, 10.0) == 0.0)", 
				"    tc = vec3(0.0, 0.0, 0.0);",
				"}",
				"result = vec4(tc, 1.0);"
				);
	};
}
