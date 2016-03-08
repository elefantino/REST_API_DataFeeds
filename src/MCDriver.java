//Copyright (c) 2015 Elena Rose, University of Tampere
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

import java.io.IOException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MCDriver {
	
    private MemcachedClient memcachedClient;
	private String url;
	private boolean is_init;
	
	public MCDriver() {
		this.is_init = false;
		this.memcachedClient = null;
	}
	
	public boolean isRunning() {
		
		if (this.memcachedClient == null) {
    		System.out.println("Starting for url: " + this.url);
            MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(this.url));
            try {
                this.memcachedClient = builder.build();
            	this.is_init = true;
            } catch (IOException e) {
                this.memcachedClient = null;
                this.is_init = false;
                e.printStackTrace();
            }
        }
		return this.is_init;
		
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void stop() {
    	
        try {
            this.memcachedClient.shutdown();
            this.is_init = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void store(String key, String value){
        
    	try {
            if(this.memcachedClient != null && key != null) {
            	System.out.println("Setting " + key + ", " + value);
                this.memcachedClient.set(key, 0, value);
            }
            else {
            	System.out.println("client or key is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
	    
}
