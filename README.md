# Konto

## Development IDE setup

* Import in IntelliJ IDEA
* Create Run Configuration, build and deploy `konto:war exploded` artifact to Wildfly 10.x
* Run GWT SDM code server in background: `mvn gwt:run-codeserver`
* Open http://localhost:8080/

## Deployment

* Make directory for installation: `mkdir .local/`
* Download dependencies JAR files: `mvn dependency:copy-dependencies -DoutputDirectory=.local/lib`
* Run database: `java -jar .local/lib/h2-1.3.158.jar -tcp -tcpPort 9093 -baseDir $PWD/.local/`
* Edit `src/main/resources/hibernate.cfg.xml`
* Either run in IDE or build and deploy WAR manually: `mvn clean package`

