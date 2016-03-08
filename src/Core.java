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

//This class includes the main processing (polling and saving data), and starts
//a thread to send files to the memcached client. 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Core {

	private final HashMap<String, String> config;
    private MCDriver mc;
    private Timer timer;
    private Timer endTimer;
    private Timer timer2;
    private Timer endTimer2;
    private DateFormat time_format;
    private Date start_date;
    
    public Core() {
        
        this.config = new HashMap<String, String>();
        this.mc = new MCDriver();
        //this.first_record = ""; 
        //this.date_format = new SimpleDateFormat("yyyy-MM-dd-HH"); //new file each hour
        this.time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.start_date = new Date();
        
        this.config.put("server_port", "" );
        this.config.put("json_path", "" );
        this.config.put("sending_delay", "30000"); //0.5 minute
        this.config.put("sending_delay_secondinterface", "600000"); //10 minutes
        this.config.put("log_file", "log_trafficdataimport.log" );
        this.config.put("life", "1" );
        
        this.timer = new Timer();
        this.endTimer = new Timer();
        
        this.timer2 = new Timer();
        this.endTimer2 = new Timer();
        
    }
	
    public void start() {
    	
        this.timer.scheduleAtFixedRate( new TimerTask() {
			@Override
			public void run() {
				processJSON();
			}}, 100, Long.parseLong(this.config.get("sending_delay")));
		
        if(!this.config.get("life").equals("-1")) {
            this.endTimer.schedule( new TimerTask() {
				@Override
				public void run() {
					shutdown();
				}},  Long.parseLong( this.config.get("life") ) * 1000 * 60 * 60 );		
        }
        
        this.timer2.scheduleAtFixedRate( new TimerTask() {
			@Override
			public void run() {
				processConfigJSON();
			}}, 1000, Long.parseLong(this.config.get("sending_config_delay")));
		
        if(!this.config.get("life").equals("-1")) {
            this.endTimer2.schedule( new TimerTask() {
				@Override
				public void run() {
					shutdown();
				}},  Long.parseLong( this.config.get("life") ) * 1000 * 60 * 60 );		
        }
        
    }
    
    public final void shutdown() {

        LogWriter.write("Core : Starting graceful shutdown... ");
        this.mc.stop();
        this.timer.cancel();
        this.timer2.cancel();
    	LogWriter.write("Core : Shutdown successfully. ");
        System.exit(0);
        
    }
    
    public void init( String settings ) {
            
        if( settings.isEmpty() || settings == null );
        else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(settings));
                String str;
		
                while ((str = in.readLine()) != null) {
                    if(str.startsWith(";") || str.isEmpty())
                        continue;
                    else {	   
                        int idx = str.indexOf("=");
                        String key = str.substring(0, idx);
                        int idx2 = str.lastIndexOf("=");
                        String value = str.substring(idx2+1, str.length());
                        this.config.put(key.trim(), value.trim());  
                    }
                }
                in.close();
            } 
            catch (IOException e) {
                System.out.println("Core : Exception occured while reading configuration file." );
                System.out.println("Core : Initialization failed.." );
                Logger.getLogger("Core").log(Level.SEVERE, null, e);
                return;
            }
        }
		
        LogWriter.init(this.config.get("log_file"), 1, 0);
		
        this.mc.setUrl(this.config.get("server_port"));		
        LogWriter.write( "Core : " + this.time_format.format(this.start_date));
        LogWriter.write( "Core : Core initialized successfully." );
        LogWriter.write( "Core : Running..." );
        
    }

    public final void processJSON() {

    	if (!this.mc.isRunning()) {
            LogWriter.write( "Core : memcached client is not initialized properly! The thread is cancelled." );
            this.timer.cancel();
        }

    	try {
        	
    		String DATA_OULU = "";
            String DATA_TRE = "";
            
            //System.out.println("checking new files...");
    		
            //read json files content into strings and delete files
            File f = new File(this.config.get("json_path"));
            File[] listOfFiles = f.listFiles();
            for (File file : listOfFiles) {
            	if (file.isFile()) {
            		//LogWriter.write(file.getName());
            		if (file.getName().startsWith("DATA_OULU")) {
                       	if (!DATA_OULU.equals(""))
                       		DATA_OULU = DATA_OULU + ",";
                       	DATA_OULU = DATA_OULU + new String(Files.readAllBytes(file.toPath()));
                       	file.delete();
                    }
                    if (file.getName().startsWith("DATA_TRE")) {
                       	if (!DATA_TRE.equals(""))
                       		DATA_TRE = DATA_TRE + ",";
                       	DATA_TRE = DATA_TRE + new String(Files.readAllBytes(file.toPath()));
                       	file.delete();
                    }
                }
            }
                
            //send strings to memcached
            if (!DATA_OULU.equals(""))
               	this.mc.store("DATA_OULU", "{\"Data\":[" + DATA_OULU + "]}");
            if (!DATA_TRE.equals(""))
               	this.mc.store("DATA_TAMPERE", "{\"Data\":[" + DATA_TRE + "]}");
            
        } 
        catch(Exception e) {
            LogWriter.write( "Core : Error occured in data sending with message: " + e.getMessage() );
        }
		
    }
    
    
    public final void processConfigJSON() {

    	if (!this.mc.isRunning()) {
            LogWriter.write( "Core : memcached client is not initialized properly! The thread is cancelled." );
            this.timer2.cancel();
        }

    	try {
        	
    		String CONF_OULU = "";
            String CONF_TRE = "";
            
            //read json files content into strings and delete files
            File f = new File(this.config.get("json_path"));
            File[] listOfFiles = f.listFiles();
            for (File file : listOfFiles) {
            	if (file.isFile()) {
            		if (file.getName().startsWith("CONF_OULU")) {
                       	if (!CONF_OULU.equals(""))
                       		CONF_OULU = CONF_OULU + ",";
                       	CONF_OULU = CONF_OULU + new String(Files.readAllBytes(file.toPath()));
                       	//config files are not deleted after sending, they are stored until the next update
                       	//for this junction since config files do not include timestamps in the names
                       	//file.delete(); 
                    }
                    if (file.getName().startsWith("CONF_TRE")) {
                       	if (!CONF_TRE.equals(""))
                       		CONF_TRE = CONF_TRE + ",";
                       	CONF_TRE = CONF_TRE + new String(Files.readAllBytes(file.toPath()));
                       	//file.delete();
                    }
                }
            }
                
            //send strings to memcached
            if (!CONF_OULU.equals(""))
               	this.mc.store("META_OULU", "{\"Meta\":[" + CONF_OULU + "]}");
            if (!CONF_TRE.equals(""))
               	this.mc.store("META_TAMPERE", "{\"Meta\":[" + CONF_TRE + "]}");
            
        } 
        catch(Exception e) {
            LogWriter.write( "Core : Error occured in data sending with message: " + e.getMessage() );
        }
		
    }
	
}
