##### OVERVIEW

Sending JSON packets to the REST API on a regular basis. Configuration file parameters.conf sets up the parameters of the program:

* `server_port` - server_ip_address:port_number of the memcached server
* `json_path` - Path where Json files are searched for sending to the memcached server
* `sending_delay` - Period of sending data to the memcached server 
* `sending_delay_secondinterface` - Period of sending data to the memcached server to the second interface. In other words, API might have several interfaces with different rate of updating. Thus, if there are two interfaces, you can start two threads with different rate of sending.
* `log_file` - Name of a log file

