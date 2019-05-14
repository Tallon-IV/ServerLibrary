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
import java.io.InputStream;
import java.io.OutputStream;

import talloniv.networking.IProtocol;
import talloniv.networking.IProtocolMessage;

public class RemoteClientConnection implements IThreadedConnection
{
	private IServerTask serverTask;
	private IProtocol protocol;
	private RemoteClient client;
	
	public RemoteClientConnection(RemoteClient client, IProtocol protocol, 
			IServerTask serverTask)
	{
		client.setRemoteClientConnection(this);
		this.client = client;
		this.protocol = protocol;
		this.serverTask = serverTask;
	}
	
	public RemoteClient getClient()
	{
		return client;
	}
	
	@Override
	public void run() 
	{
		try 
		{
			byte buffer[] = new byte[2048];
			int bytes = -1;
			InputStream in = client.GetSocket().getInputStream();
			while (true)
			{
				bytes = in.read(buffer);
				if (bytes < 1)
				{
					continue;
				}
				
				if (!protocol.Verify(buffer))
				{
					System.out.println("Malformed message");
					continue;
				}
				
				serverTask.OnDataReceived(client, protocol.Deserialise(buffer));
			}
		}
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
			serverTask.OnDisconnected(client);
		}
		
	}
	
	@Override
	public void Send(IProtocolMessage message) 
	{
		try 
		{
			OutputStream out = client.GetSocket().getOutputStream();
			out.write(protocol.Serialise(message));
			out.flush();
			serverTask.OnDataSent(client, message);
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
			serverTask.OnDisconnected(client);
		}
	}
	
	public static class RemoteClientConnectionFactory
	{
		private IProtocol protocol;
		private IServerTask serverTask;
		
		public RemoteClientConnectionFactory(IProtocol protocol, IServerTask serverTask)
		{
			this.protocol = protocol;
			this.serverTask = serverTask;
		}
		
		public RemoteClientConnection SpawnConnection(RemoteClient client)
		{
			return new RemoteClientConnection(client, protocol, serverTask);
		}
	}

}
