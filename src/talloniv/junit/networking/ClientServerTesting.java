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

package talloniv.junit.networking;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import talloniv.networking.IProtocolMessage;
import talloniv.networking.server.IServerTask;
import talloniv.networking.server.RemoteClient;
import talloniv.networking.server.ServerBlockingThread;

class ClientServerTesting {

	private Socket socket = null;
	private ServerBlockingThread server = null;
	@BeforeEach
	void setUp() throws Exception 
	{
		server = new ServerBlockingThread(8888, true);
		server.Start();
	}

	@AfterEach
	void tearDown() throws Exception 
	{
		server.Stop();
		socket.close();
	}

	@Test
	@Disabled("One test at a time.")
	void initialConnectionTest() throws Exception 
	{
		socket = new Socket("localhost", 8888);
		OutputStream out = socket.getOutputStream();
		out.write("Haters Sit Down".getBytes());
		out.flush();
		Thread.sleep(1000);
		assertEquals(1, server.GetActiveConnectionsCount());
	}
	
	@Test
	@Disabled("One test at a time.")
	void rejectConnectionTest() throws Exception
	{
		server.AddServerTask(new IServerTask() {

			@Override
			public boolean OnConnect(RemoteClient client) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void OnConnected(RemoteClient client) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnDataReceived(RemoteClient client, IProtocolMessage data) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnDataSent(RemoteClient client, IProtocolMessage data) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnDisconnected(RemoteClient client) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		socket = new Socket("localhost", 8888);
		InputStream in = socket.getInputStream();
		byte buf[] = new byte[2048];
		in.read(buf);
		assertTrue(new String(buf).trim().equals("Rejected by Server."), 
				"Server did not reject this connection.");
	}
	
	@Test
	void echoMessageTest() throws Exception
	{
		server.AddServerTask(new IServerTask() {

			@Override
			public boolean OnConnect(RemoteClient client) {
				return true;
			}

			@Override
			public void OnConnected(RemoteClient client) {
				System.out.println("Server: Remote client at "+client.GetIpAddress()+"connected!");
			}

			@Override
			public void OnDataReceived(RemoteClient client, IProtocolMessage data) 
			{
				String extractedData = new String((byte[])data.Extract()).trim();
				System.out.println("Server: Client "+client.GetIpAddress()+" says ->"
			                       +extractedData);
				//EchoBack
				System.out.println("Server: Echoing back client message...");
				client.Send(data);
			}

			@Override
			public void OnDataSent(RemoteClient client, IProtocolMessage data) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnDisconnected(RemoteClient client) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		socket = new Socket("localhost", 8888);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		String s = "Memes for lyfe.";
		out.write(s.getBytes());
		out.flush();
		byte buf[] = new byte[2048];
		in.read(buf);
		assertTrue(new String(buf).trim().equals(s), 
				"Server did not reply with an echo of the originally sent message.");
	}
}
