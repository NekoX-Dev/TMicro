// ---------------------------------------------------------------------------
// jWebSocket - JSON Token Processor
// Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.packetProcessors;

import j2me.util.Collection;
import j2me.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.token.Token;

/**
 * converts JSON formatted data packets into tokens and vice versa.
 *
 * @author Alexander Schulze, Roderick Baier (improvements regarding JSON
 * array).
 */
public class JSONProcessor {

	/**
	 * converts a JSON formatted data packet into a token.
	 *
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPacket aDataPacket) {
		Token lToken = new Token();
		try {
			String lStr = aDataPacket.getString("UTF-8");
			JSONTokener jsonTokener = new JSONTokener(lStr);
			JSONObject jsonObject = new JSONObject(jsonTokener);
			for (Enumeration lEnum = jsonObject.keys(); lEnum.hasMoreElements();) {
				String lKey = (String) lEnum.nextElement();
				lToken.put(lKey, jsonObject.get(lKey));
			}
		} catch (UnsupportedEncodingException ex) {
			// TODO: add exception handling
			// log.error(ex.getClass().getName() + ": " + ex.getMessage());
		} catch (JSONException ex) {
			// TODO: add exception handling
			// log.error(ex.getClass().getName() + ": " + ex.getMessage());
		}
		return lToken;
	}

	public static WebSocketPacket tokenToPacket(Token token) {
		WebSocketPacket packet = null;
		try {
			JSONObject json = tokenToJSON(token);
			String data = json.toString();
			packet = new RawPacket(data, "UTF-8");
		} catch (JSONException ex) {
			// log.error(ex.getClass().getName() + ": " + ex.getMessage());
		} catch (UnsupportedEncodingException ex) {
			// log.error(ex.getClass().getName() + ": " + ex.getMessage());
		}

		return packet;
	}

	private static JSONObject tokenToJSON(Token token) throws JSONException {
		JSONObject json = new JSONObject();

		Iterator lIterator = token.getKeys();
		while (lIterator.hasNext()) {
			String key = (String) lIterator.next();
			Object value = token.get(key);
			if (value instanceof Collection) {
				JSONArray lArray = new JSONArray();
				Iterator lItems = ((Collection) value).iterator();
				while (lItems.hasNext()) {
					Object lItem = lItems.next();
					if (lItem instanceof Token) {
						JSONObject object = tokenToJSON((Token) lItem);
						lArray.put(object);
					} else {
						lArray.put(lItem);
					}
				}
				json.put(key, lArray);
			} else if (value instanceof Token) {
				json.put(key, tokenToJSON((Token) value));
			} else if (value instanceof Object[]) {
				JSONArray lArray = new JSONArray();
				Object[] lObjs = (Object[]) value;
				for (int lIdx = 0; lIdx < lObjs.length; lIdx++) {
					Object lObj = lObjs[lIdx];
					lArray.put(lObj);
				}
				json.put(key, lArray);
			} else {
				json.put(key, value);
			}
		}

		return json;
	}
}
