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

import talloniv.networking.IProtocol;

public interface IServer extends Runnable 
{
	public void Start();
	public void Stop();
	public void SetProtocol(IProtocol protocol);
	public void AddServerTask(IServerTask task);
	public void RemoveServerTask(IServerTask task);
	public void RemoveServerTaskByClass(Class<IServerTask> taskClass);
	public boolean IsOnline();
	public String GetIp();
	public int GetPort();
	public int GetActiveConnectionsCount();
	public int GetRemoteClients(); //????
	public long GetOnlineTime();
}
