/*
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */package ch.fhnw.ether.examples.raytracing;

import ch.fhnw.ether.camera.Camera;
import ch.fhnw.ether.camera.ICamera;
import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.EventDrivenScheduler;
import ch.fhnw.ether.examples.raytracing.surface.Plane;
import ch.fhnw.ether.examples.raytracing.surface.Sphere;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.IView.ViewType;
import ch.fhnw.ether.view.gl.DefaultView;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

public class RaytracerExample1 {

	public static void main(String[] args) {
		new RaytracerExample1();
	}

	public RaytracerExample1() {

		// create scene objects
		ICamera cam = new Camera(2.5f, 1, 0.5f, Float.POSITIVE_INFINITY);
		ILight l = new PointLight(new Vec3(0, 0, 3), RGBA.WHITE);
		ParametricScene s = new ParametricScene(cam, l);
		RayTraceObject chugeli = new RayTraceObject(new Sphere(0.5f));
		RayTraceObject bode = new RayTraceObject(new Plane());

		// setup scene
		s.addMesh(chugeli);
		s.addMesh(bode);

		// adjust scene
		chugeli.setPosition(Vec3.Z.scale(0.5f));
		cam.move(0, 0, 1, false);

		// use default controller
		IController c = new DefaultController(new EventDrivenScheduler(), new RayTracingRenderer(s)) {
			@Override
			public void updateUI() {
			} // UI needs forward renderer
		};

		IView v = new DefaultView(c, 100, 100, 100, 100, ViewType.INTERACTIVE_VIEW, "Raytracing", cam);

		c.addView(v);
		c.setScene(s);
	}

}
