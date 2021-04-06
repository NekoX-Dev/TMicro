//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Client for Java Micro Edition (J2ME)
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

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;
import org.jwebsocket.listener.WebSocketClientEvent;

/**
 *
 * @author Alexander Schulze
 */
public class BaseClientJ2ME extends BaseClient {

	private boolean mIsRunning = false;
	private Thread mInboundThread;
	private InboundProcess mInboundProcess;
	private SocketConnection mSocket = null;
	private InputStream mIn = null;
	private OutputStream mOut = null;

	public BaseClientJ2ME() {
	}

	public void open(String aURL) throws WebSocketException {
		try {
			// validate URL, Java ME expects socket:// 
			if (aURL.startsWith("ws:")) {
				aURL = "socket" + aURL.substring(2);
			} else if (aURL.startsWith("http:")) {
				aURL = "socket" + aURL.substring(4);
			}
			mSocket = (SocketConnection) Connector.open(aURL, Connector.READ_WRITE);
			// "socket://localhost:8787"

			mIn = mSocket.openInputStream();
			mOut = mSocket.openOutputStream();

			// send handshake to server
			byte[] lReq = WebSocketHandshake.generateC2SRequest("localhost:8787", "/");
			mOut.write(lReq);
			mOut.flush();

			// wait on handshake response
			byte[] lBuff = WebSocketHandshake.readS2CResponse(mIn);

			// parse handshake response from server
			// Map lResp = WebSocketHandshake.parseS2CResponse(lBuff);

			mInboundProcess = new InboundProcess();
			mInboundThread = new Thread(mInboundProcess);
			mInboundThread.start();

		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when opening WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void send(String aData, String aEncoding) throws WebSocketException {
		try {
			mOut.write(0x00);
			mOut.write(aData.getBytes(aEncoding));
			mOut.write(0xff);
			mOut.flush();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when sending via WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void send(byte[] aData) throws WebSocketException {
		try {
			mOut.write(0x00);
			mOut.write(aData);
			mOut.write(0xff);
			mOut.flush();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName() + " when sending via WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void received(String aData, String aEncoding) throws WebSocketException {
	}

	public void received(byte[] aData) throws WebSocketException {
	}

	public void close() throws WebSocketException {
		mIsRunning = false;
		try {
			mOut.close();
			mIn.close();
			mSocket.close();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getName()
					+ " when closing WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private class InboundProcess implements Runnable {

		public void run() {
			mIsRunning = true;
			byte[] lBuff = new byte[MAX_FRAME_SIZE];
			int lPos = -1;
			int lStart = -1;

			WebSocketClientEvent lEvent = new WebSocketClientEvent();
			notifyOpened(lEvent);

			while (mIsRunning) {
				try {
					int b = mIn.read();
					// start of frame
					if (b == 0x00) {
						lPos = 0;
						lStart = 0;
						// end of frame
					} else if (b == 0xff) {
						if (lStart >= 0) {
							byte[] lBA = new byte[lPos];
							System.arraycopy(lBuff, 0, lBA, 0, lPos);
							received(lBA);
							lEvent = new WebSocketClientEvent();
							WebSocketPacket lPacket = new RawPacket(lBA);
							notifyPacket(lEvent, lPacket);
						}
						lStart = -1;
						// end of stream
					} else if (b < 0) {
						mIsRunning = false;
						// any other byte within or outside a frame
					} else {
						if (lStart >= 0) {
							lBuff[lPos] = (byte) b;
						}
						lPos++;
					}
				} catch (Exception ex) {
					mIsRunning = false;
					// throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}

			lEvent = new WebSocketClientEvent();
			notifyClosed(lEvent);
		}
	}

	public boolean isConnected() {
		return mIsRunning;
	}
}
