/**
 * MIT License
 * 
 * Copyright (c) 2019 Gershon Fosu (tallon-iv)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package talloniv.networking.server;

import java.net.Socket;

import talloniv.networking.IProtocolMessage;

public class RemoteClient implements IRemoteClient
{
	private Socket socket;
	private RemoteClientConnection connection = null;
	private String ipAddress;
	private int port;
	private long connectedWhen;
	private long connectionTime;
	
	public RemoteClient(String ipAddress, int port)
	{
		RemoteClient(ipAddress, port);
	}
	
	public RemoteClient(Socket socket)
	{
		this.socket = socket;
		RemoteClient(socket.getInetAddress().getHostAddress(), socket.getPort());
	}

	private void RemoteClient(String ipAddress, int port) 
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.connectedWhen = System.currentTimeMillis();
		this.connectionTime = 0;
	}
	
	void setRemoteClientConnection(RemoteClientConnection connection)
	{
		this.connection = connection;
	}
	
	public Socket GetSocket()
	{
		return socket;
	}
	
	public String GetIpAddress()
	{
		return ipAddress;
	}
	
	public int GetPort()
	{
		return port;
	}
	
	public long GetConnectedWhen()
	{
		return connectedWhen;
	}
	
	public long GetConnectionTime()
	{
		return connectionTime;
	}
	
	public void updateConnectionTime()
	{
		connectionTime = System.currentTimeMillis() - connectedWhen;
	}

	@Override
	public void Send(IProtocolMessage msg) 
	{
		if (connection == null)
		{
			return;
		}
		
		connection.Send(msg);
	}
}
