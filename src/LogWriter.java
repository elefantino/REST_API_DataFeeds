//Copyright (c) 2015 University of Tampere
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

import java.io.FileWriter;
import java.io.IOException;

public final class LogWriter {
	private static FileWriter fw;
	private static boolean is_silent;
	
	public LogWriter() {
		
	}
	
	public static void init( String file, int purge, int silent ) {
		try
		{
			boolean append = false;
			if( purge == 0)
				append = true;
			else if( purge == 1)
				append = false;
			
			if( silent == 0)
				is_silent = false;
			else if( silent == 1 )
				is_silent = true;
			
		    LogWriter.fw = new FileWriter(file, append );
		}
		catch(IOException e)
		{
			System.out.println("ERROR");
		    System.out.println("IOException: " + e.getMessage());
		}		
	}
	
	public static void write( String message ) {
		try {
			LogWriter.fw.write( message + "\n\r" );
			
			fw.flush();
			
			if( !LogWriter.is_silent )
				System.out.println( message );
			
		} catch (IOException e) {
			System.out.println( "Core:Logger : Error writing to file." );
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		try {
			LogWriter.fw.close();
		} catch (IOException e) {
			System.out.println( "Core:Logger : Error closing the file." );
			e.printStackTrace();
		}
	}
}
