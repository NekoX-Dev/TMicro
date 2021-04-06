//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.kit;

import j2me.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javolution.util.FastMap;

/**
 *
 * @author Alexander Schulze
 */
public class WebSocketHandshake {

	public static int MAX_HEADER_SIZE = 16834;

	/**
	 * Generates the initial handshake request from a client to the jWebSocket
	 * Server. This is send from a Java client to the server when a connection
	 * is about to be established. The browser's implement that internally.
	 *
	 * @param aURI
	 * @return
	 */
	// public static byte[] generateC2SRequest(URI aURI) {
	public static byte[] generateC2SRequest(String lHost, String lPath) {
		// String lPath = aURI.getPath();
		// String lHost = aURI.getHost();
		String lOrigin = "http://" + lHost;
		String lHandshake = "GET " + lPath + " HTTP/1.1\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Host: " + lHost + "\r\n"
				+ "Origin: " + lOrigin + "\r\n"
				+ "\r\n";
		byte[] lBA = null;
		try {
			lBA = lHandshake.getBytes("US-ASCII");
		} catch (Exception ex) {
		}
		return lBA;
	}

	private static long calcSecKeyNum(String aKey) {
		StringBuffer lSB = new StringBuffer();
		// StringBuuffer lSB = new StringBuuffer();
		int lSpaces = 0;
		for (int i = 0; i < aKey.length(); i++) {
			char lC = aKey.charAt(i);
			if (lC == ' ') {
				lSpaces++;
			} else if (lC >= '0' && lC <= '9') {
				lSB.append(lC);
			}
		}
		long lRes = -1;
		if (lSpaces > 0) {
			try {
				lRes = Long.parseLong(lSB.toString()) / lSpaces;
//				log.debug("Key: " + aKey + ", Numbers: " + lSB.toString() + ", Spaces: " + lSpaces + ", Result: " + lRes);
			} catch (NumberFormatException ex) {
				// use default result
			}
		}
		return lRes;
	}

	/**
	 * Parses the response from the client on an initial client's handshake
	 * request. This is always performed on the server only when a client -
	 * irrespective of if it is a Java Client or Browser Client - initiates a
	 * connection.
	 *
	 * @param aResp
	 * @return
	 */
	public static Map parseC2SRequest(byte[] aResp) {
		String lHost = null;
		String lOrigin = null;
		String lLocation = null;
		String lPath = null;
		String lSecKey1 = null;
		String lSecKey2 = null;
		byte[] lSecKey3 = new byte[8];
		boolean lIsSecure = false;
		long lSecNum1 = -1;
		long lSecNum2 = -1;
		byte[] lSecKeyResp = new byte[8];

		FastMap lRes = new FastMap();

		int lRespLen = aResp.length;
		String lResp = "";
		try {
			lResp = new String(aResp, "US-ASCII");
		} catch (Exception ex) {
			// TODO: add exception handling
		}

		if (lResp.indexOf("policy-file-request") >= 0) { // "<policy-file-request/>"
			lRes.put("policy-file-request", lResp);
			return lRes;
		}

		lIsSecure = (lResp.indexOf("Sec-WebSocket") > 0);

		if (lIsSecure) {
			lRespLen -= 8;
			for (int i = 0; i < 8; i++) {
				lSecKey3[i] = aResp[lRespLen + i];
			}
		}

		// now parse header for correct handshake....
		// get host....
		int lPos = lResp.indexOf("Host:");
		lPos += 6;
		lHost = lResp.substring(lPos);
		lPos = lHost.indexOf("\r\n");
		lHost = lHost.substring(0, lPos);
		// get origin....
		lPos = lResp.indexOf("Origin:");
		lPos += 8;
		lOrigin = lResp.substring(lPos);
		lPos = lOrigin.indexOf("\r\n");
		lOrigin = lOrigin.substring(0, lPos);
		// get path....
		lPos = lResp.indexOf("GET");
		lPos += 4;
		lPath = lResp.substring(lPos);
		lPos = lPath.indexOf("HTTP");
		lPath = lPath.substring(0, lPos - 1);

		lLocation = "ws://" + lHost + lPath;

		// the following section implements the sec-key process in WebSocket Draft 76
		/*
		 * To prove that the handshake was received, the server has to take
		 * three pieces of information and combine them to form a response. The
		 * first two pieces of information come from the |Sec-WebSocket-Key1|
		 * and |Sec-WebSocket-Key2| fields in the client handshake.
		 *
		 * Sec-WebSocket-Key1: 18x 6]8vM;54 *(5: { U1]8 z [ 8
		 * Sec-WebSocket-Key2: 1_ tx7X d < nw 334J702) 7]o}` 0
		 *
		 * For each of these fields, the server has to take the digits from the
		 * value to obtain a number (in this case 1868545188 and 1733470270
		 * respectively), then divide that number by the number of spaces
		 * characters in the value (in this case 12 and 10) to obtain a 32-bit
		 * number (155712099 and 173347027). These two resulting numbers are
		 * then used in the server handshake, as described below.
		 */
		lPos = lResp.indexOf("Sec-WebSocket-Key1:");
		if (lPos > 0) {
			lPos += 20;
			lSecKey1 = lResp.substring(lPos);
			lPos = lSecKey1.indexOf("\r\n");
			lSecKey1 = lSecKey1.substring(0, lPos);
			lSecNum1 = calcSecKeyNum(lSecKey1);
//			log.debug("Sec-WebSocket-Key1:" + secKey1 + " => " + secNum1);
		}
		lPos = lResp.indexOf("Sec-WebSocket-Key2:");
		if (lPos > 0) {
			lPos += 20;
			lSecKey2 = lResp.substring(lPos);
			lPos = lSecKey2.indexOf("\r\n");
			lSecKey2 = lSecKey2.substring(0, lPos);
			lSecNum2 = calcSecKeyNum(lSecKey2);
//			log.debug("Sec-WebSocket-Key2:" + secKey2 + " => " + secNum2);
		}

		/*
		 * The third piece of information is given after the fields, in the last
		 * eight bytes of the handshake, expressed here as they would be seen if
		 * interpreted as ASCII: Tm[K T2u The concatenation of the number
		 * obtained from processing the |Sec- WebSocket-Key1| field, expressed
		 * as a big-endian 32 bit number, the number obtained from processing
		 * the |Sec-WebSocket-Key2| field, again expressed as a big-endian 32
		 * bit number, and finally the eight bytes at the end of the handshake,
		 * form a 128 bit string whose MD5 sum is then used by the server to
		 * prove that it read the handshake.
		 */

		if (lSecNum1 != -1 && lSecNum2 != -1) {

//			log.debug("Sec-WebSocket-Key3:" + new String(secKey3, "UTF-8"));
			//BigInteger sec1 = new BigInteger();
			//BigInteger sec2 = new BigInteger(lSecNum2.toString());

			// concatenate 3 parts secNum1 + secNum2 + secKey
			byte[] l128Bit = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			byte[] lTmp;
			long secTmp;

			secTmp = lSecNum1;
			// TODO: replace by arraycopy
			for (int i = 0; i < 4; i++) {
				l128Bit[i] = (byte) (secTmp & 0xff);
				secTmp >>= 8;
			}
			secTmp = lSecNum2;
			for (int i = 0; i < 4; i++) {
				l128Bit[i + 4] = (byte) (secTmp & 0xff);
			}
			lTmp = lSecKey3;
			// TODO: replace by arraycopy
			for (int i = 0; i < 8; i++) {
				l128Bit[i + 8] = lTmp[i];
			}
			// build md5 sum of this new 128 byte string
			try {
				// MessageDigest md = MessageDigest.getInstance("MD5");
				// md.update(l128Bit, 0, 32);
				// md.digest(lSecKeyResp, 0, 32);
			} catch (Exception ex) {
//				log.error("getMD5: " + ex.getMessage());
			}
		}

		lRes.put("path", lPath);
		lRes.put("host", lHost);
		lRes.put("origin", lOrigin);
		lRes.put("location", lLocation);
		lRes.put("secKey1", lSecKey1);
		lRes.put("secKey2", lSecKey2);

		lRes.put("isSecure", new Boolean(lIsSecure));
		lRes.put("secKeyResponse", lSecKeyResp);

		return lRes;
	}

	/**
	 * Generates the response for the server to answer an initial client
	 * request. This is performed on the server only as an answer to a client's
	 * request - irrespective of if it is a Java or Browser Client.
	 *
	 * @param aRequest
	 * @return
	 */
	public static byte[] generateS2CResponse(FastMap aRequest) {
		String lPolicyFileRequest = (String) aRequest.get("policy-file-request");
		if (lPolicyFileRequest != null) {
			byte[] lBA;
			try {
				lBA = ("<cross-domain-policy>"
						+ "<allow-access-from domain=\"*\" to-ports=\"*\" />"
						+ "</cross-domain-policy>\n").getBytes("US-ASCII");
			} catch (UnsupportedEncodingException ex) {
				lBA = null;
			}
			return lBA;
		}

		// now that we have parsed the header send handshake...
		// since 0.9.0.0609 considering Sec-WebSocket-Key processing
		boolean lIsSecure = ((Boolean) aRequest.get("isSecure")).booleanValue();
		String lOrigin = (String) aRequest.get("origin");
		String lLocation = (String) aRequest.get("location");
		String lRes =
				// since IETF draft 76 "WebSocket Protocol" not "Web Socket Protocol"
				// change implemented since v0.9.5.0701
				"HTTP/1.1 101 Web" + (lIsSecure ? "" : " ") + "Socket Protocol Handshake\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ (lIsSecure ? "Sec-" : "") + "WebSocket-Origin: " + lOrigin + "\r\n"
				+ (lIsSecure ? "Sec-" : "") + "WebSocket-Location: " + lLocation + "\r\n"
				+ "\r\n";

		byte[] lBA;
		try {
			lBA = lRes.getBytes("US-ASCII");
			// if Sec-WebSocket-Keys are used send security response first
			if (lIsSecure) {
				byte[] lSecKey = (byte[]) aRequest.get("secKeyResponse");
				byte[] lResult = new byte[lBA.length + lSecKey.length];
				System.arraycopy(lBA, 0, lResult, 0, lBA.length);
				System.arraycopy(lSecKey, 0, lResult, lBA.length, lSecKey.length);
				return lResult;
			} else {
				return lBA;
			}
		} catch (UnsupportedEncodingException ex) {
			return null;
		}

	}

	/**
	 * Reads the handshake response from the server into an byte array. This is
	 * used on clients only. The browser client implement that internally.
	 *
	 * @param aIS
	 * @return
	 */
	public static byte[] readS2CResponse(InputStream aIS) {
		byte[] lBuff = new byte[MAX_HEADER_SIZE];
		boolean lContinue = true;
		int lIdx = 0;
		int lB1 = 0, lB2 = 0, lB3 = 0, lB4 = 0;
		while (lContinue && lIdx < MAX_HEADER_SIZE) {
			int b;
			try {
				b = aIS.read();
				if (b < 0) {
					return null;
				}
			} catch (IOException ex) {
				return null;
			}
			// build mini queue to check for \r\n\r\n sequence in handshake
			lB1 = lB2;
			lB2 = lB3;
			lB3 = lB4;
			lB4 = b;
			lContinue = !(lB1 == 13 && lB2 == 10 && lB3 == 13 && lB4 == 10);
			lBuff[lIdx] = (byte) b;
			lIdx++;
		}
		byte[] lRes = new byte[lIdx];
		System.arraycopy(lBuff, 0, lRes, 0, lIdx);
		return lRes;
	}

	/*
	 * Parses the websocket handshake response from the server. This is
	 * performed on Java Client only, the browsers implement that internally.
	 * @param aResp @return
	 */
	public static FastMap parseS2CResponse(byte[] aResp) {
		FastMap lRes = new FastMap();
		String lResp = null;
		try {
			lResp = new String(aResp, "US-ASCII");
		} catch (Exception ex) {
			// TODO: add exception handling
		}
		return lRes;
	}
}
