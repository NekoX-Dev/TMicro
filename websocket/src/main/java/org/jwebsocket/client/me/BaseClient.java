//	---------------------------------------------------------------------------
//	jWebSocket - Abstract WebSocket Client
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.me;

import j2me.util.Iterator;
import j2me.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.listener.WebSocketClientEvent;

/**
 *
 * @author Alexander Schulze
 */
public abstract class BaseClient implements WebSocketClient {

	private List mListeners = new FastList();

	/*
	 * The connection has not yet been established.
	 */
	public static final int CONNECTING = 0;
	/*
	 * The WebSocket connection is established and communication is possible.
	 */
	public static final int OPEN = 1;
	/*
	 * The connection is going through the closing handshake.
	 */
	public static final int CLOSING = 2;
	/*
	 * The connection has been closed or could not be opened.
	 */
	public static final int CLOSED = 3;
	/*
	 * The maximum amount of bytes per frame
	 */
	public static final int MAX_FRAME_SIZE = 16384;

	// @Override
	public void addListener(WebSocketClientListener aListener) {
		mListeners.add(aListener);
	}

	// @Override
	public void removeListener(WebSocketClientListener aListener) {
		mListeners.remove(aListener);
	}

	// @Override
	public List getListeners() {
		return mListeners;
	}

	// @Override
	public void notifyOpened(WebSocketClientEvent aEvent) {
		Iterator lIterator = getListeners().iterator();
		while (lIterator.hasNext()) {
			WebSocketClientListener lListener = (WebSocketClientListener) lIterator.next();
			lListener.processOpened(aEvent);
		}
	}

	// @Override
	public void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		Iterator lIterator = getListeners().iterator();
		while (lIterator.hasNext()) {
			WebSocketClientListener lListener = (WebSocketClientListener) lIterator.next();
			lListener.processPacket(aEvent, aPacket);
		}
	}

	// @Override
	public void notifyClosed(WebSocketClientEvent aEvent) {
		Iterator lIterator = getListeners().iterator();
		while (lIterator.hasNext()) {
			WebSocketClientListener lListener = (WebSocketClientListener) lIterator.next();
			lListener.processClosed(aEvent);
		}
	}
}
