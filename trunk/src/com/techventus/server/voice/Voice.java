/*
 * Voice.java
 *
 * Created: Sat Mar  13 14:41:11 2010
 *
 * Copyright (C) 2010 Techventus, LLC
 * 
 * Techventus, LLC is not responsible for any use or misuse of this product.
 * In using this software you agree to hold harmless Techventus, LLC and any other
 * contributors to this project from any damages or liabilities which might result 
 * from its use.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.techventus.server.voice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.techventus.server.voice.datatypes.Phone;

/**
 * The Class Voice. This class is the basis of the entire API and contains all
 * the components necessary to connect and authenticate with Google Voice, place
 * calls and SMS, and pull in the raw data from the account.
 * 
 * @author Techventus, LLC
 */
public class Voice {

	public boolean PRINT_TO_CONSOLE;
	public List<Phone> phoneList = null;
	String general = null;
	String rnrSEE = null;
	String source = null;
	String user = null;
	String pass = null;
	String authToken = null;
	static String generalURLString = "https://www.google.com/voice/";
	static String inboxURLString = "https://www.google.com/voice/inbox/recent/inbox/";
	static String starredURLString = "https://www.google.com/voice/inbox/recent/starred/";
	static String recentAllURLString = "https://www.google.com/voice/inbox/recent/all/";
	static String spamURLString = "https://www.google.com/voice/inbox/recent/spam/";
	static String trashURLString = "https://www.google.com/voice/inbox/recent/spam/";
	static String voicemailURLString = "https://www.google.com/voice/inbox/recent/voicemail/";
	static String smsURLString = "https://www.google.com/voice/inbox/recent/sms/";
	static String recordedURLString = "https://www.google.com/voice/inbox/recent/recorded/";
	static String placedURLString = "https://www.google.com/voice/inbox/recent/placed/";
	static String receivedURLString = "https://www.google.com/voice/inbox/recent/received/";
	static String missedURLString = "https://www.google.com/voice/inbox/recent/missed/";
	static String phoneEnableURLString = "https://www.google.com/voice/settings/editDefaultForwarding/";

	/**
	 * Instantiates a new voice. This constructor is deprecated. Try
	 * Voice(String user, String pass) which automatically determines rnrSee and
	 * assigns a source.
	 * 
	 * @param user
	 *            the user
	 * @param pass
	 *            the pass
	 * @param source
	 *            the source
	 * @param rnrSee
	 *            the rnr see
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Deprecated
	public Voice(String user, String pass, String source, String rnrSee)
			throws IOException {

		this.user = user;
		this.pass = pass;
		this.rnrSEE = rnrSee;
		this.source = source;

		login();
	}

	/**
	 * A constructor which which allows a custom source. The purpose of the
	 * source variable is currently unknown.
	 * 
	 * @param user
	 *            the username in the format of user@gmail.com or user@googlemail.com
	 * @param pass
	 *            the password
	 * @param source
	 *            the source
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Voice(String user, String pass, String source) throws IOException {
		init(user, pass, source, true);

	}

	/**
	 * Instantiates a new Voice Object. This is generally the simplest and
	 * preferred constructor. This Constructor enables verbose output.
	 * 
	 * @param user
	 *            the username in the format of user@gmail.com or user@googlemail.com
	 * @param pass
	 *            the pass
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Voice(String user, String pass) throws IOException {
		init(user, pass, null, true);
	}

	/**
	 * Instantiates a new voice. Custom Source Variable allowed, and
	 * printDebugIntoSystemOut which allows for Verbose output.
	 * 
	 * @param user
	 *            the username in the format of user@gmail.com or user@googlemail.com
	 * @param pass
	 *            the password
	 * @param source
	 *            the arbitrary source identifier.  Can be anything.
	 * @param printDebugIntoToSystemOut
	 *            the print debug into to system out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Voice(String user, String pass, String source,
			boolean printDebugIntoToSystemOut) throws IOException {
		init(user, pass, source, printDebugIntoToSystemOut);
	}

	/**
	 * Internal function used by all constructors to fully initiate the Voice
	 * Object.
	 * 
	 * @param user
	 *            the username in the format of user@gmail.com or user@googlemail.com
	 * @param pass
	 *            the password for the google account
	 * @param source
	 *            the source
	 * @param printDebugIntoToSystemOut
	 *            the print debug into to system out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void init(String user, String pass, String source,
			boolean printDebugIntoToSystemOut) throws IOException {
		this.PRINT_TO_CONSOLE = printDebugIntoToSystemOut;
		this.user = user;
		this.pass = pass;
		// this.rnrSEE = rnrSee;
		if (source != null) {
			this.source = source;
		} else {
			this.source = "GoogleVoiceJava";
		}

		login();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		general = getGeneral();
		setRNRSEE();
		setPhoneInfo();
	}

	// public Voice(){
	// authToken = "abcde";
	// }

	/**
	 * Fetches and returns the raw page source code for the Inbox.
	 * 
	 * @return the inbox
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getInbox() throws IOException {
		return get(inboxURLString);
	}

	/**
	 * Fetches the page Source Code for the Voice homepage. This file contains
	 * most of the useful information for the Google Voice Account such as
	 * attached Phone info and Contacts.
	 * 
	 * @return the general
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getGeneral() throws IOException {
		return get(generalURLString);
	}

	/**
	 * Gets the raw page source code for the starred items.
	 * 
	 * @return the starred item page source
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getStarred() throws IOException {
		return get(starredURLString);
	}

	/**
	 * Gets the raw page source code for the recent items.
	 * 
	 * @return the recent raw source code
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getRecent() throws IOException {
		return get(recentAllURLString);
	}

	/**
	 * Gets the page source for the spam.
	 * 
	 * @return the spam
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getSpam() throws IOException {
		return get(spamURLString);
	}

	/**
	 * Gets the page source for the recorded calls.
	 * 
	 * @return the recorded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getRecorded() throws IOException {
		return get(recordedURLString);
	}

	/**
	 * Gets the raw source code for the placed calls page.
	 * 
	 * @return the placed calls source code
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getPlaced() throws IOException {
		return get(placedURLString);
	}

	/**
	 * Gets the received calls source code.
	 * 
	 * @return the received
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getReceived() throws IOException {
		return get(receivedURLString);
	}

	/**
	 * Gets the missed calls source code.
	 * 
	 * @return the missed
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getMissed() throws IOException {
		return get(missedURLString);
	}

	/**
	 * Gets the SMS page raw source code.
	 * 
	 * @return the sMS
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getSMS() throws IOException {
		return get(smsURLString);
	}

	/**
	 * Internal method which parses the Homepage source code to determine the
	 * rnrsee variable, this variable is passed into most fuctions for placing
	 * calls and sms.
	 */
	private void setRNRSEE() {
		if (general != null) {
			String p1 = general.split("'_rnr_se': '", 2)[1];
			rnrSEE = p1.split("',", 2)[0];
			p1 = null;
		}
	}

	/**
	 * Reads raw account info, and creates the phoneList of Phone objects.
	 */
	private void setPhoneInfo() {
		if (general != null) {
			List<Phone> phoneList = new ArrayList<Phone>();
			String p1 = general.split("'phones':", 2)[1];
			p1 = (p1.split("'_rnr_se'", 2))[0];
			String[] a = p1.split("\\{\"id\"\\:");
			// if(PRINT_TO_CONSOLE) System.out.println(a[0]);
			for (int i = 1; i < a.length; i++) {
				Phone phone = new Phone();
				String[] b = a[i].split(",\"wd\"\\:\\{", 2)[0].split(",");
				phone.id = Integer.parseInt(b[0].replaceAll("\"", ""));
				for (int j = 0; j < b.length; j++) {
					if (b[j].contains("phoneNumber")) {
						phone.number = b[j].split("\\:")[1]
								.replaceAll("\"", "");
					} else if (b[j].contains("type")) {
						phone.type = b[j].split("\\:")[1].replaceAll("\"", "");
					} else if (b[j].contains("name")) {
						phone.name = b[j].split("\\:")[1].replaceAll("\"", "");

					} else if (b[j].contains("formattedNumber")) {
						phone.formattedNumber = b[j].split("\\:")[1]
								.replaceAll("\"", "");
					} else if (b[j].contains("carrier")) {
						phone.carrier = b[j].split("\\:")[1].replaceAll("\"",
								"");
					} else if (b[j].contains("\"verified")) {
						phone.verified = Boolean
								.parseBoolean(b[j].split("\\:")[1].replaceAll(
										"\"", ""));
					}
				}
				phoneList.add(phone);
			}

			this.phoneList = phoneList;

		}
	}

	/**
	 * Place a call.
	 * 
	 * @param originNumber
	 *            the origin number
	 * @param destinationNumber
	 *            the destination number
	 * @param phoneType
	 *            the phone type
	 * @return the raw response string received from Google Voice.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String call(String originNumber, String destinationNumber,
			String phoneType) throws IOException {
		String out = "";
		String calldata = URLEncoder.encode("auth", "UTF-8") + "="
				+ URLEncoder.encode(authToken, "UTF-8");
		calldata += "&" + URLEncoder.encode("outgoingNumber", "UTF-8") + "="
				+ URLEncoder.encode(destinationNumber, "UTF-8");
		calldata += "&" + URLEncoder.encode("forwardingNumber", "UTF-8") + "="
				+ URLEncoder.encode(originNumber, "UTF-8");
		calldata += "&" + URLEncoder.encode("subscriberNumber", "UTF-8") + "="
				+ URLEncoder.encode("undefined", "UTF-8");
		calldata += "&" + URLEncoder.encode("phoneType", "UTF-8") + "="
				+ URLEncoder.encode(phoneType, "UTF-8");
		calldata += "&" + URLEncoder.encode("remember", "UTF-8") + "="
				+ URLEncoder.encode("0", "UTF-8");
		calldata += "&" + URLEncoder.encode("_rnr_se", "UTF-8") + "="
				+ URLEncoder.encode(rnrSEE, "UTF-8");
		// POST /voice/call/connect/ outgoingNumber=[number to
		// call]&forwardingNumber=[forwarding
		// number]&subscriberNumber=undefined&remember=0&_rnr_se=[pull from
		// page]
		URL callURL = new URL("https://www.google.com/voice/call/connect/");

		URLConnection callconn = callURL.openConnection();
		callconn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		callconn.setDoOutput(true);
		OutputStreamWriter callwr = new OutputStreamWriter(callconn
				.getOutputStream());
		callwr.write(calldata);
		callwr.flush();

		BufferedReader callrd = new BufferedReader(new InputStreamReader(
				callconn.getInputStream()));

		String line;
		while ((line = callrd.readLine()) != null) {
			out += line + "\n\r";

		}

		callwr.close();
		callrd.close();

		if (out.equals("")) {
			throw new IOException("No Response Data Received.");
		}

		return out;

	}

	/**
	 * Cancel a call that was just placed.
	 * 
	 * @param originNumber
	 *            the origin number
	 * @param destinationNumber
	 *            the destination number
	 * @param phoneType
	 *            the phone type
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String cancelCall(String originNumber, String destinationNumber,
			String phoneType) throws IOException {
		String out = "";
		String calldata = URLEncoder.encode("auth", "UTF-8") + "="
				+ URLEncoder.encode(authToken, "UTF-8");
		calldata += "&" + URLEncoder.encode("outgoingNumber", "UTF-8") + "="
				+ URLEncoder.encode("undefined", "UTF-8");
		calldata += "&" + URLEncoder.encode("forwardingNumber", "UTF-8") + "="
				+ URLEncoder.encode("undefined", "UTF-8");

		calldata += "&" + URLEncoder.encode("cancelType", "UTF-8") + "="
				+ URLEncoder.encode("C2C", "UTF-8");
		calldata += "&" + URLEncoder.encode("_rnr_se", "UTF-8") + "="
				+ URLEncoder.encode(rnrSEE, "UTF-8");
		// POST /voice/call/connect/ outgoingNumber=[number to
		// call]&forwardingNumber=[forwarding
		// number]&subscriberNumber=undefined&remember=0&_rnr_se=[pull from
		// page]
		URL callURL = new URL("https://www.google.com/voice/call/cancel/");

		URLConnection callconn = callURL.openConnection();
		callconn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		callconn.setDoOutput(true);
		OutputStreamWriter callwr = new OutputStreamWriter(callconn
				.getOutputStream());
		callwr.write(calldata);
		callwr.flush();

		BufferedReader callrd = new BufferedReader(new InputStreamReader(
				callconn.getInputStream()));

		String line;
		while ((line = callrd.readLine()) != null) {
			out += line + "\n\r";

		}

		callwr.close();
		callrd.close();

		if (out.equals("")) {
			throw new IOException("No Response Data Received.");
		}

		return out;

	}

	/**
	 * Disable one of the the phones attached to the account from ringing.
	 * Requires the internal ID for that phone, as an integer, usually 1,2,3,
	 * etc.
	 * 
	 * @param ID
	 *            the iD
	 * @return the raw response of the disable action.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String phoneDisable(int ID) throws IOException {
		String out = "";

		String disabledata = URLEncoder.encode("auth", "UTF-8") + "="
				+ URLEncoder.encode(authToken, "UTF-8");
		disabledata += "&" + URLEncoder.encode("enabled", "UTF-8") + "="
				+ URLEncoder.encode("0", "UTF-8");
		disabledata += "&" + URLEncoder.encode("phoneId", "UTF-8") + "="
				+ URLEncoder.encode(Integer.toString(ID), "UTF-8");
		disabledata += "&" + URLEncoder.encode("_rnr_se", "UTF-8") + "="
				+ URLEncoder.encode(rnrSEE, "UTF-8");
		// POST /voice/call/connect/ outgoingNumber=[number to
		// call]&forwardingNumber=[forwarding
		// number]&subscriberNumber=undefined&remember=0&_rnr_se=[pull from
		// page]

		//
		URL disableURL = new URL(phoneEnableURLString);

		if (PRINT_TO_CONSOLE)
			System.out.println(disabledata);

		URLConnection disableconn = disableURL.openConnection();
		disableconn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		disableconn.setDoOutput(true);
		disableconn.setDoInput(true);

		OutputStreamWriter callwr = new OutputStreamWriter(disableconn
				.getOutputStream());
		callwr.write(disabledata);
		callwr.flush();

		BufferedReader callrd = new BufferedReader(new InputStreamReader(
				disableconn.getInputStream()));

		String line;
		while ((line = callrd.readLine()) != null) {
			out += line + "\n\r";

		}

		callwr.close();
		callrd.close();

		if (out.equals("")) {
			throw new IOException("No Response Data Received.");
		}

		return out;

	}

	/**
	 * Enable one of the the phones attached to the account from ringing.
	 * Requires the internal ID for that phone, as an integer, usually 1,2,3,
	 * etc.
	 * 
	 * @param ID
	 *            the iD
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String phoneEnable(int ID) throws IOException {
		String out = "";

		String disabledata = URLEncoder.encode("auth", "UTF-8") + "="
				+ URLEncoder.encode(authToken, "UTF-8");
		disabledata += "&" + URLEncoder.encode("enabled", "UTF-8") + "="
				+ URLEncoder.encode("1", "UTF-8");
		disabledata += "&" + URLEncoder.encode("phoneId", "UTF-8") + "="
				+ URLEncoder.encode(Integer.toString(ID), "UTF-8");
		disabledata += "&" + URLEncoder.encode("_rnr_se", "UTF-8") + "="
				+ URLEncoder.encode(rnrSEE, "UTF-8");
		// POST /voice/call/connect/ outgoingNumber=[number to
		// call]&forwardingNumber=[forwarding
		// number]&subscriberNumber=undefined&remember=0&_rnr_se=[pull from
		// page]

		//
		URL enableURL = new URL(phoneEnableURLString);

		if (PRINT_TO_CONSOLE)
			System.out.println(disabledata);

		URLConnection enableconn = enableURL.openConnection();
		enableconn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		enableconn.setDoOutput(true);
		enableconn.setDoInput(true);

		OutputStreamWriter callwr = new OutputStreamWriter(enableconn
				.getOutputStream());
		callwr.write(disabledata);
		callwr.flush();

		BufferedReader callrd = new BufferedReader(new InputStreamReader(
				enableconn.getInputStream()));

		String line;
		while ((line = callrd.readLine()) != null) {
			out += line + "\n\r";

		}

		callwr.close();
		callrd.close();

		if (out.equals("")) {
			throw new IOException("No Response Data Received.");
		}

		return out;

	}

	/**
	 * Send an SMS
	 * 
	 * @param destinationNumber
	 *            the destination number
	 * @param txt
	 *            the Text of the message. Messages longer than the allowed
	 *            character length will be split into multiple messages.
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String sendSMS(String destinationNumber, String txt)
			throws IOException {
		String out = "";
		String smsdata = URLEncoder.encode("auth", "UTF-8") + "="
				+ URLEncoder.encode(authToken, "UTF-8");

		smsdata += "&" + URLEncoder.encode("phoneNumber", "UTF-8") + "="
				+ URLEncoder.encode(destinationNumber, "UTF-8");
		smsdata += "&" + URLEncoder.encode("text", "UTF-8") + "="
				+ URLEncoder.encode(txt, "UTF-8");
		smsdata += "&" + URLEncoder.encode("_rnr_se", "UTF-8") + "="
				+ URLEncoder.encode(rnrSEE, "UTF-8");
		URL smsurl = new URL("https://www.google.com/voice/sms/send/");

		URLConnection smsconn = smsurl.openConnection();
		smsconn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		smsconn.setDoOutput(true);
		OutputStreamWriter callwr = new OutputStreamWriter(smsconn
				.getOutputStream());
		callwr.write(smsdata);
		callwr.flush();

		BufferedReader callrd = new BufferedReader(new InputStreamReader(
				smsconn.getInputStream()));

		String line;
		while ((line = callrd.readLine()) != null) {
			out += line + "\n\r";

		}

		callwr.close();
		callrd.close();

		if (out.equals("")) {
			throw new IOException("No Response Data Received.");
		}

		return out;
	}

	/**
	 * HTML GET request for a given URL String.
	 * 
	 * @param urlString
	 *            the url string
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	String get(String urlString) throws IOException {
		URL url = new URL(urlString + "?auth="
				+ URLEncoder.encode(authToken, "UTF-8"));
		URLConnection conn = url.openConnection();
		conn
				.setRequestProperty(
						"User-agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n\r");
		}
		rd.close();
		String result = sb.toString();

		return result;
	}

	/**
	 * Login Method to refresh authentication with Google Voice.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void login() throws IOException {

		String data = URLEncoder.encode("accountType", "UTF-8") + "="
				+ URLEncoder.encode("GOOGLE", "UTF-8");
		data += "&" + URLEncoder.encode("Email", "UTF-8") + "="
				+ URLEncoder.encode(user, "UTF-8");
		data += "&" + URLEncoder.encode("Passwd", "UTF-8") + "="
				+ URLEncoder.encode(pass, "UTF-8");
		data += "&" + URLEncoder.encode("service", "UTF-8") + "="
				+ URLEncoder.encode("grandcentral", "UTF-8");
		data += "&" + URLEncoder.encode("source", "UTF-8") + "="
				+ URLEncoder.encode(source, "UTF-8");

		// Send data
		URL url = new URL("https://www.google.com/accounts/ClientLogin");
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		String line;

		// String AuthToken = null;
		while ((line = rd.readLine()) != null) {
			// if(PRINT_TO_CONSOLE) System.out.println(line);
			if (line.contains("Auth=")) {
				this.authToken = line.split("=", 2)[1].trim();
				if (PRINT_TO_CONSOLE)
					System.out.println("AUTH TOKEN =" + this.authToken);
			}
		}
		wr.close();
		rd.close();

		if (this.authToken == null) {
			throw new IOException("No Authorisation Received.");
		}
	}

	/**
	 * Fires a Get request for Recent Items. If the Response requests login
	 * authentication or if an exception is thrown, a false is returned,
	 * otherwise if arbitrary text is contained for a logged in account, a true
	 * is returned.
	 * 
	 *TODO Examine methodology.
	 * 
	 * @return true, if is logged in
	 */
	public boolean isLoggedIn() {
		String res;
		try {
			res = getRecent();
		} catch (IOException e) {
			return false;
		}
		if (res
				.contains("<meta name=\"description\" content=\"Google Voice gives you one number")
				&& res
						.contains("action=\"https://www.google.com/accounts/ServiceLoginAuth?service=grandcentral\"")) {
			return false;
		} else {
			if (res.contains("Enter a new or existing contact name")
					|| res.contains("<json><![CDATA[")) {
				return true;
			}
		}
		return false;
	}

}