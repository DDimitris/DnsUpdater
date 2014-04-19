default:
	javac -classpath .:classes:lib/* code/src/DnsDaemon.java -d bin/
	cp lib/* dnsdaemon/libs
clean:
	rm -rf bin/*
