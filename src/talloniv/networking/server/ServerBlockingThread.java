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

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import talloniv.networking.IProtocol;
import talloniv.networking.IProtocolMessage;
import talloniv.networking.server.RemoteClientConnection.RemoteClientConnectionFactory;

public class ServerBlockingThread implements IServer, IServerTask
{
	private boolean isOnline = false;
	private long upTime = 0;
	public Thread serverThread = null;
	private IProtocol protocol = null;
	private RemoteClientConnectionFactory connectionFactory;
	private ServerSocket srvSocket = null;
	private final ArrayList<IServerTask> serverTasks = new ArrayList<IServerTask>();
	private final ArrayList<RemoteClientConnection> remoteClients = 
			new ArrayList<RemoteClientConnection>();
	
	public ServerBlockingThread() throws IOException
	{
		ServerBlockingThread(0, false);
	}
	
	public ServerBlockingThread(IProtocol protocol) throws IOException
	{
		ServerBlockingThread(0, false, protocol);
	}
	
	public ServerBlockingThread(IServerTask...serverTasks) throws IOException
	{
		ServerBlockingThread(0, false);
		addServerTasks(serverTasks);
	}
	
	public ServerBlockingThread(IProtocol protocol, IServerTask...serverTasks)
			throws IOException
	{
		ServerBlockingThread(0, false, protocol);
		addServerTasks(serverTasks);
	}
	
	public ServerBlockingThread(int port, boolean strict) throws IOException
	{
		ServerBlockingThread(port, strict);
	}
	
	public ServerBlockingThread(int port, boolean strict, IProtocol protocol) 
			throws IOException
	{
		ServerBlockingThread(port, strict, protocol);
	}
	
	public ServerBlockingThread(int port, boolean strict, IServerTask...serverTasks)
			throws IOException
	{
		ServerBlockingThread(port, strict);
		addServerTasks(serverTasks);
	}
	
	public ServerBlockingThread(int port, boolean strict, IProtocol protocol, 
			IServerTask...serverTasks) throws IOException
	{
		ServerBlockingThread(port, strict, protocol);
		addServerTasks(serverTasks);
	}
	
	private void ServerBlockingThread(int port, boolean strict) throws IOException
	{
		try
		{
			srvSocket = new ServerSocket(port);
		}
		catch(IOException ioe)
		{
			if (strict)
			{
				throw ioe;
			}
			
			srvSocket = new ServerSocket();
		}
		
		this.protocol = getDefaultProtocol();
	}
	
	private void ServerBlockingThread(int port, boolean strict, IProtocol protocol)
			throws IOException
	{
		ServerBlockingThread(port, strict);
		if (protocol != null)
		{
			this.protocol = protocol;
		}
	}
	
	private IProtocol getDefaultProtocol()
	{
		return new IProtocol() {

			@Override
			public boolean Verify(byte[] data) {
				return true;
			}

			@Override
			public IProtocolMessage Deserialise(byte[] data) {
				return new IProtocolMessage() {

					@Override
					public Object Extract() {
						return data;
					}

					@Override
					public Object Pack() {
						return data;
					}
					
				};
			}

			@Override
			public byte[] Serialise(IProtocolMessage data) {
				return (byte[])data.Pack();
			}
			
		};
	}
	private void addServerTasks(IServerTask[] serverTasks)
	{
		for (int i = 0; i < serverTasks.length; i++)
		{
			this.serverTasks.add(serverTasks[i]);
		}
	}
	
	private void addClient(RemoteClientConnection remoteClientConn)
	{
		//Why is there a method for this again???
		remoteClients.add(remoteClientConn);
	}
	
	private void rejectClient(RemoteClient rejectedClient, String message)
	{
		Socket rejectedSock = rejectedClient.GetSocket();
		try {
			OutputStream out = rejectedSock.getOutputStream();
			out.write(message.getBytes());
			out.flush();
			rejectedSock.shutdownInput();
			rejectedSock.shutdownOutput();
			out.close();
			rejectedSock.close();
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	public void run() 
	{
		upTime = System.currentTimeMillis();
		connectionFactory = new RemoteClientConnectionFactory(protocol, this);
		while (isOnline)
		{
			try 
			{
				System.out.println("Listening for new connection...");
				Socket incomingConnection = srvSocket.accept();
				RemoteClient client = new RemoteClient(incomingConnection);
				if (!OnConnect(client))
				{
					rejectClient(client, "Rejected by Server.");
					continue;
				}
				RemoteClientConnection clientConnection = connectionFactory.SpawnConnection(client);
				new Thread(clientConnection).start();
				addClient(clientConnection);
			} 
			catch (IOException e) 
			{
				System.out.println("Server Shutting Down:"+e.getMessage());
				for (int i = 0; i < remoteClients.size(); i++)
				{
					RemoteClientConnection remoteClientConn = remoteClients.get(i);
					rejectClient(remoteClientConn.getClient(), "Server Shutting Down");
				}
				remoteClients.clear();
			}
		}
	}

	@Override
	public void Start() 
	{
		isOnline = true;
		serverThread = new Thread(this);
		serverThread.start();
	}

	@Override
	public void Stop() 
	{
		isOnline = false;
		try 
		{
			srvSocket.close();
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
		}
	}

	@Override
	public boolean IsOnline() 
	{
		return isOnline;
	}

	@Override
	public String GetIp() 
	{
		return srvSocket.getInetAddress().getHostAddress();
	}

	@Override
	public int GetPort() 
	{
		return srvSocket.getLocalPort();
	}

	@Override
	public int GetActiveConnectionsCount() 
	{
		return remoteClients.size();
	}

	@Override
	public int GetRemoteClients() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long GetOnlineTime() 
	{
		if (!isOnline)
		{
			return 0;
		}
		
		return System.currentTimeMillis() - upTime;
	}

	@Override
	public void SetProtocol(IProtocol protocol) 
	{
		this.protocol = protocol;
	}

	@Override
	public void AddServerTask(IServerTask task) 
	{
		serverTasks.add(task);
	}

	@Override
	public void RemoveServerTask(IServerTask task) 
	{
		serverTasks.remove(task);
	}

	@Override
	public void RemoveServerTaskByClass(Class<IServerTask> taskClass) 
	{
		for (int i = 0; i < serverTasks.size(); i++)
		{
			if (serverTasks.get(i).getClass() == taskClass)
			{
				serverTasks.remove(i);
			}
		}
	}
	
	@Override
	public boolean OnConnect(RemoteClient client) 
	{
		boolean accept = true;
		for (int i = 0; i < serverTasks.size(); i++)
		{
			accept = serverTasks.get(i).OnConnect(client);
		}
		return accept;
	}

	@Override
	public void OnConnected(RemoteClient client) 
	{
		for (int i = 0; i < serverTasks.size(); i++)
		{
			serverTasks.get(i).OnConnected(client);
		}
	}

	@Override
	public void OnDataReceived(RemoteClient client, IProtocolMessage data) 
	{
		for (int i = 0; i < serverTasks.size(); i++)
		{
			serverTasks.get(i).OnDataReceived(client, data);
		}
	}

	@Override
	public void OnDataSent(RemoteClient client, IProtocolMessage data) 
	{
		for (int i = 0; i < serverTasks.size(); i++)
		{
			serverTasks.get(i).OnDataSent(client, data);
		}
	}

	@Override
	public void OnDisconnected(RemoteClient client) 
	{
		for (int i = 0; i < serverTasks.size(); i++)
		{
			serverTasks.get(i).OnDisconnected(client);
		}
		
		for (int i = 0; i < remoteClients.size(); i++)
		{
			RemoteClientConnection remoteClientConn = remoteClients.get(i);
			if (remoteClientConn.getClient() == client)
			{
				remoteClients.remove(i);
				break;
			}
		}
	}

}
