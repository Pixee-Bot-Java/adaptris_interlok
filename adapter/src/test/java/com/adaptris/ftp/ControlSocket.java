/*
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003 Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 *
 *  $Id: ControlSocket.java,v 1.4 2009/07/08 13:51:47 lchan Exp $
 */

package com.adaptris.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.filetransfer.FileTransferException;

/**
 * Supports client-side FTP operations
 *
 * @author Bruce Blackshaw
 * @deprecated since 2.8.2, this is only used by {@link FtpClient}
 */
@Deprecated
class ControlSocket {

  /**
   * Standard FTP end of line sequence
   */
  static final String EOL = "\r\n";

  /**
   * The control port number for FTP
   */
  static final int CONTROL_PORT = 21;

  /**
   * Used to flag messages
   */
  private static final String DEBUG_ARROW = "---> ";

  /**
   * Start of password message
   */
  private static final String PASSWORD_MESSAGE = DEBUG_ARROW + "PASS";

  /**
   * Controls if responses sent back by the server are sent to assigned output
   * stream
   */
  private boolean debugResponses = false;

  /**
   * The underlying socket.
   */
  private Socket controlSock = null;

  /**
   * The write that writes to the control socket
   */
  private Writer writer = null;

  /**
   * The reader that reads control data from the control socket
   */
  private BufferedReader reader = null;
  private int timeout;
  private static transient Logger logR = LoggerFactory.getLogger(FtpClient.class);

  /**
   * Constructor. Performs TCP connection and sets up reader/writer. Allows
   * different control port to be used
   *
   * @param remoteHost Remote hostname
   * @param controlPort port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public ControlSocket(String remoteHost, int controlPort, int timeout) throws IOException, FileTransferException {
    this(InetAddress.getByName(remoteHost), controlPort, timeout);
  }

  /**
   * Constructor. Performs TCP connection and sets up reader/writer. Allows
   * different control port to be used
   *
   * @param remoteAddr Remote inet address
   * @param controlPort port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public ControlSocket(InetAddress remoteAddr, int controlPort, int timeout) throws IOException, FileTransferException {

    // ensure we get debug from initial connection sequence
    debugResponses(true);
    controlSock = createSocket(remoteAddr, controlPort, timeout);
    setTimeout(timeout);
    initStreams();
    validateConnection();
    // switch off debug - user can switch on from this point
    debugResponses(false);
  }

  /**
   * Checks that the standard 220 reply is returned following the initiated
   * connection
   */
  private void validateConnection() throws IOException, FileTransferException {

    String reply = readReply();
    validateReply(reply, "220");
  }

  /**
   * Obtain the reader/writer streams for this connection
   */
  private void initStreams() throws IOException {

    // input stream
    InputStream is = controlSock.getInputStream();
    reader = new BufferedReader(new InputStreamReader(is));

    // output stream
    OutputStream os = controlSock.getOutputStream();
    writer = new OutputStreamWriter(os);
  }

  /**
   * Get the name of the remote host
   *
   * @return remote host name
   */
  String getRemoteHostName() {
    InetAddress addr = controlSock.getInetAddress();
    return addr.getHostName();
  }

  /**
   * Set the TCP timeout on the underlying control socket.
   *
   * If a timeout is set, then any operation which takes longer than the timeout
   * value will be killed with a java.io.InterruptedException.
   *
   * @param millis The length of the timeout, in milliseconds
   */
  void setTimeout(int millis) throws IOException {

    if (controlSock == null) {
      throw new IllegalStateException("Failed to set timeout - " + "no control socket");
    }

    controlSock.setSoTimeout(millis);
    timeout = millis;
  }

  /**
   * Quit this FTP session and clean up.
   *
   * @throws IOException if a comms error occurs
   *
   */
  void logout() throws IOException {

    IOException ex = null;
    try {
      writer.close();
    }
    catch (IOException e) {
      ex = e;
    }
    try {
      reader.close();
    }
    catch (IOException e) {
      ex = e;
    }
    try {
      controlSock.close();
    }
    catch (IOException e) {
      ex = e;
    }
    if (ex != null) {
      throw ex;
    }
  }

  /**
   * Request a data socket be created on the server, connect to it and return our connected socket.
   *
   * @param connectMode the mode.
   * @return connected data socket
   */
  DataSocket createDataSocket(FtpDataMode connectMode) throws IOException, FileTransferException {

    if (connectMode == FtpDataMode.ACTIVE) {
      return new DataSocket(createDataSocketActive());
    }
    else { // PASV
      return new DataSocket(createDataSocketPASV());
    }
  }

  /**
   * Request a data socket be created on the Client client on any free port, do
   * not connect it to yet.
   *
   * @return not connected data socket
   */
  private ServerSocket createDataSocketActive() throws IOException, FileTransferException {

    // use any available port
    ServerSocket socket = new ServerSocket(0);

    // get the local address to which the control socket is bound.
    InetAddress localhost = controlSock.getLocalAddress();

    // send the PORT command to the server
    setDataPort(localhost, (short) socket.getLocalPort());

    return socket;
  }

  /**
   * Helper method to convert a byte into an unsigned short value
   *
   * @param value value to convert
   * @return the byte value as an unsigned short
   */
  private short toUnsignedShort(byte value) {
    return value < 0 ? (short) (value + 256) : (short) value;
  }

  /**
   * Convert a short into a byte array
   *
   * @param value value to convert
   * @return a byte array
   */
  protected byte[] toByteArray(short value) {

    byte[] bytes = new byte[2];
    bytes[0] = (byte) (value >> 8); // bits 1- 8
    bytes[1] = (byte) (value & 0x00FF); // bits 9-16
    return bytes;
  }

  /**
   * Sets the data port on the server, i.e. sends a PORT command
   *
   * @param host the local host the server will connect to
   * @param portNo the port number to connect to
   */
  private void setDataPort(InetAddress host, short portNo) throws IOException, FileTransferException {

    byte[] hostBytes = host.getAddress();
    byte[] portBytes = toByteArray(portNo);

    // assemble the PORT command
    String cmd = new StringBuffer("PORT ").append(toUnsignedShort(hostBytes[0])).append(",").append(toUnsignedShort(hostBytes[1]))
        .append(",").append(toUnsignedShort(hostBytes[2])).append(",").append(toUnsignedShort(hostBytes[3])).append(",").append(
            toUnsignedShort(portBytes[0])).append(",").append(toUnsignedShort(portBytes[1])).toString();

    // send command and check reply
    String reply = sendCommand(cmd);
    validateReply(reply, "200");
  }

  /**
   * Request a data socket be created on the server, connect to it and return
   * our connected socket.
   *
   * @return connected data socket
   */
  private Socket createDataSocketPASV() throws IOException, FileTransferException {

    // PASSIVE command - tells the server to listen for
    // a connection attempt rather than initiating it
    String reply = sendCommand("PASV");
    validateReply(reply, "227");

    // The reply to PASV is in the form:
    // 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2).
    // where h1..h4 are the IP address to connect and
    // p1,p2 the port number
    // Example:
    // 227 Entering Passive Mode (128,3,122,1,15,87).
    // NOTE: PASV command in IBM/Mainframe returns the string
    // 227 Entering Passive Mode 128,3,122,1,15,87 (missing
    // brackets)

    // extract the IP data string from between the brackets
    int startIP = reply.indexOf('(');
    int endIP = reply.indexOf(')');

    // allow for IBM missing brackets around IP address
    if (startIP < 0 && endIP < 0) {
      startIP = reply.toUpperCase().lastIndexOf("MODE") + 4;
      endIP = reply.length();
    }

    String ipData = reply.substring(startIP + 1, endIP);
    int[] parts = new int[6];

    int len = ipData.length();
    int partCount = 0;
    StringBuffer buf = new StringBuffer();

    // loop thru and examine each char
    for (int i = 0; i < len && partCount <= 6; i++) {

      char ch = ipData.charAt(i);
      if (Character.isDigit(ch)) {
        buf.append(ch);
      }
      else if (ch != ',') {
        throw new FileTransferException("Malformed PASV reply: " + reply);
      }

      // get the part
      if (ch == ',' || i + 1 == len) { // at end or at separator
        try {
          parts[partCount++] = Integer.parseInt(buf.toString());
          buf.setLength(0);
        }
        catch (NumberFormatException ex) {
          throw new FileTransferException("Malformed PASV reply: " + reply);
        }
      }
    }

    // assemble the IP address
    // we try connecting, so we don't bother checking digits etc
    String ipAddress = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];

    // assemble the port number
    int port = (parts[4] << 8) + parts[5];

    // create the socket
    return createSocket(InetAddress.getByName(ipAddress), port, timeout);
  }

  private static Socket createSocket(InetAddress ipAddress, int port, int millis) throws IOException {
    InetSocketAddress addr = new InetSocketAddress(ipAddress, port);
    Socket result = new Socket();
    result.connect(addr, millis);
    return result;
  }

  /**
   * Send a command to the FTP server and return the server's reply
   *
   * @return reply to the supplied command
   */
  String sendCommand(String command) throws IOException {

    log(DEBUG_ARROW + command);

    // send it
    writer.write(command + EOL);
    writer.flush();

    // and read the result
    return readReply();
  }

  /**
   * Read the FTP server's reply to a previously issued command. RFC 959 states
   * that a reply consists of the 3 digit code followed by text. The 3 digit
   * code is followed by a hyphen if it is a muliline response, and the last
   * line starts with the same 3 digit code.
   *
   * @return reply string
   */
  String readReply() throws IOException {

    String firstLine = reader.readLine();
    if (firstLine == null || firstLine.length() == 0) {
      throw new IOException("Unexpected null reply received");
    }

    StringBuffer reply = new StringBuffer(firstLine);

    log(reply.toString());

    String replyCode = reply.toString().substring(0, 3);

    // check for multiline response and build up
    // the reply
    if (reply.charAt(3) == '-') {

      boolean complete = false;
      while (!complete) {
        String line = reader.readLine();
        if (line == null) {
          throw new IOException("Unexpected null reply received");
        }

        log(line);

        if (line.length() > 3 && line.substring(0, 3).equals(replyCode) && line.charAt(3) == ' ') {
          reply.append(line.substring(3));
          complete = true;
        }
        else { // not the last line
          reply.append(" ");
          reply.append(line);
        }
      } // end while
    } // end if
    return reply.toString();
  }

  /**
   * Validate the response the host has supplied against the expected reply. If
   * we get an unexpected reply we throw an exception, setting the message to
   * that returned by the FTP server
   *
   * @param reply the entire reply string we received
   * @param expectedReplyCode the reply we expected to receive
   *
   */
  Reply validateReply(String reply, String expectedReplyCode) throws IOException, FtpException {

    // all reply codes are 3 chars long
    String replyCode = reply.substring(0, 3);
    String replyText = reply.substring(4);
    Reply replyObj = new Reply(replyCode, replyText);

    if (replyCode.equals(expectedReplyCode)) {
      return replyObj;
    }

    // if unexpected reply, throw an exception
    throw new FtpException(replyText, replyCode);
  }

  /**
   * Validate the response the host has supplied against the expected reply. If
   * we get an unexpected reply we throw an exception, setting the message to
   * that returned by the FTP server
   *
   * @param reply the entire reply string we received
   * @param expectedReplyCodes array of expected replies
   * @return an object encapsulating the server's reply
   *
   */
  Reply validateReply(String reply, String[] expectedReplyCodes) throws IOException, FtpException {

    // all reply codes are 3 chars long
    String replyCode = reply.substring(0, 3);
    String replyText = reply.substring(4);

    Reply replyObj = new Reply(replyCode, replyText);

    for (int i = 0; i < expectedReplyCodes.length; i++) {
      if (replyCode.equals(expectedReplyCodes[i])) {
        return replyObj;
      }
    }

    // got this far, not recognised
    throw new FtpException(replyText, replyCode);
  }

  /**
   * Switch debug of responses on or off
   *
   * @param on true if you wish to have responses to stdout, false otherwise
   */
  void debugResponses(boolean on) {
    debugResponses = on;
  }

  /**
   * Log a message, if logging is set up
   *
   * @param msg message to log
   */
  void log(String msg) {
    if (debugResponses) {
      if (!msg.startsWith(PASSWORD_MESSAGE)) {
        logR.trace(msg);
      }
      else {
        logR.trace(PASSWORD_MESSAGE + " ********");
      }
    }
  }

}
