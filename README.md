# DnsUpdater
This is a simple daemon that updates your DNS server record every time your dynamic ip changes.

### Execution Info:
You must have maven installed because this is a maven project.
In order to install this app you need to run the following:

'''
cd scripts ; sudo bash install.sh
'''

This command will compile the project, create a target/ directory and move all tha needed files to the appropriate locations.
Also you must install the jsvc using the following command:

'''
sudo apt-get install jsvc
'''

Jsvc is a library from Apache that is used in order to run Java deamonized applications.
