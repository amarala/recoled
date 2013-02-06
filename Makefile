SRCCLASSPATH=.:../lib/jsdt.jar:../lib/recplaj.jar
TOPCLASSPATH=src:lib/jsdt.jar:lib/recplaj.jar:lib/jmf.jar
SOURCES=cowrat/recoled/shared/*.java cowrat/recoled/server/*.java \
	cowrat/recoled/client/*.java
DOCS=doc
SOURCEDIR=src
DISTDIR=recoled-`cat VERSION`
BINDISTDIR=recoled-`cat VERSION`-bin
BINDISTFILES=lib/*.jar README
DISTFILES=BUGS COPYING ChangeLog INSTALL Makefile README TODO VERSION AUTHORS exclude.txt \
	doc lib src
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAVAFLAGS=-classpath $(TOPCLASSPATH)
JAVACFLAGS=-classpath $(SRCCLASSPATH)
JAVADOCFLAGS=-classpath $(SRCCLASSPATH) -d $(DOCS) -sourcepath $(SOURCEDIR)
SJARCLASSES=cowrat/recoled/server/*.class cowrat/recoled/shared/*.class images/
SJAR=lib/recoled-server.jar
SJARMF=ServerManifest.mf
CJARCLASSES=cowrat/recoled/client/*.class cowrat/recoled/shared/*.class images/
CJAR=lib/recoled-client.jar
CJARMF=ClientManifest.mf

build:
	cd $(SOURCEDIR) && $(JAVAC) $(JAVACFLAGS) $(SOURCES)

docs:
	javadoc $(JAVADOCFLAGS) -subpackages cowrat.recoled

serverjar:
	cd $(SOURCEDIR) && jar cmf $(SJARMF) ../$(SJAR) $(SJARCLASSES) 

clientjar:
	cd $(SOURCEDIR) && jar cmf $(CJARMF) ../$(CJAR) $(CJARCLASSES) 

jar: build serverjar clientjar
	echo 'created lib/recoled-server.jar and lib/recoled-client.jar'
	echo 'do "cd lib ; java -jar recoled-server.jar" to run'

srcdist:
	mkdir $(DISTDIR)
	cp -a $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR)-src.tar.gz $(DISTDIR) --exclude-from=exclude-src.txt
	rm -rf $(DISTDIR)

bindist: jar
	mkdir $(BINDISTDIR)
	cp -a $(BINDISTFILES) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz $(BINDISTDIR)
	rm -rf $(BINDISTDIR)

dist: docs jar
	mkdir $(DISTDIR)
	cp -a $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz $(DISTDIR) --exclude-from=exclude.txt
	rm -rf $(DISTDIR)

