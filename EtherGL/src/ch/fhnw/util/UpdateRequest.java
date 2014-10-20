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
 */

package ch.fhnw.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class for simple update hand-shaking.
 * 
 * @author radar
 */
public final class UpdateRequest implements IUpdateListener {
	private final AtomicBoolean update = new AtomicBoolean();

	public UpdateRequest() {
	}

	public UpdateRequest(boolean requestInitialUpdate) {
		if (requestInitialUpdate)
			requestUpdate();
	}

	/**
	 * Called to request an update.
	 * 
	 * @param source
	 *            reference to caller (ignored).
	 */
	@Override
	public void requestUpdate(Object source) {
		update.set(true);
	}

	/**
	 * Called to request an update.
	 */
	public void requestUpdate() {
		update.set(true);
	}

	/**
	 * Called to check for an update and clear a pending request.
	 * 
	 * @return true if update is required, false otherwise.
	 */
	public boolean needsUpdate() {
		return update.getAndSet(false);
	}
	
	@Override
	public String toString() {
		return update.toString();
	}
}