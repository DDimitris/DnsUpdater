default:
	javac -classpath .:classes:lib/* src/DnsDaemon.java -d bin/
	cp lib/* dnsdaemon/libs
clean:
	rm -rf bin/*
