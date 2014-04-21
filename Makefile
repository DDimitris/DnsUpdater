default:
	javac -classpath .:classes:lib/* src/daemon/DnsDaemon.java src/daemon/ReadDnsConfigFile.java -d bin/
	cp lib/* dnsdaemon/libs
clean:
	rm -rf bin/*
