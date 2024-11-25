# DB-Utils

Db-Utils ist eine Sammlung von Werkzeugen, die bei typischen Datenbankaufgaben in deinem Projekt helfen.
Es unterstützt automatische, inkrementelle SQL-Updates von Datenbanktabellen, den Export und Import von Daten
sowie ein einfaches Datenbankabfragefenster. 

Aktuell gibe es Unterstützung für Microsoft SQL und HSQLDB out-of-the-box,
aber es ist nicht schwer, die Komponente für andere Datenbanktypen zu erweitern.

## Konzepte

Die wichtigste Funktion von DB-Utils ist wahrscheinlich das automatische Update deiner Datenbank,
wann immer du ein Deployment durchführst. Zusätzlich können Daten aus deinen Datenbanken einfach
in Excel- oder Zip-Dateien exportiert oder importiert werden, und einfache Abfragen können direkt
über ein Db-Utils-GUI in deiner Anwendung ausgeführt werden.

Durch die Definition eines Resolvers, der dein Projekt-Setup bereitstellt, sowie einigen Einstellungen in globalen Variablen und möglicherweise einem Prozessstart-Event-Bean, kannst du alle Funktionen von DB-Utils nutzen.

### Inkrementelle Updates

Mit Db-Utils kannst Du eine Liste inkrementeller SQL-Skripte sowie deren Ausführungsstatus zusammen mit deinem Projekt und der zugehörigen Datenbank verwalten. Wenn Db-Utils zum ersten Mal ausgeführt wird (entweder über die GUI oder automatisch beim Start deiner Anwendung), wird eine Tabelle erstellt, um diese Liste zu verwalten. Der Tabellenname lässt sich in deinem `DbUtilsResolver` anpassen, wobei der Standardname `DbUtilsScripts` lautet. Die SQL-Skripte können manuell über die Db-Utils-GUI oder automatisch bei jedem Start deiner Anwendung ausgeführt werden. Sie können entweder in einem Dateisystemordner oder in einem Ressourcenverzeichnis (Classpath) gespeichert werden – letzteres ist die bevorzugte Methode.

Es wird empfohlen, die inkrementellen Dateien deines Projekts im Classpath deines Projekts abzulegen, zum Beispiel in einem Unterordner des `src`-Verzeichnisses (z.B. `src/resources/sql/incremental`). Außerdem solltest du ein einheitliches Namensschema für deine Skripte verwenden, wie z.B. 

`YYYYMMDD-HHMM-Ticket-Short-Description.sql`

Db-Utils erstellt eine Tabelle, in der gespeichert wird, welche dieser SQL-Skripte ausgeführt wurden, und bietet eine grafische Benutzeroberfläche, in der die Liste der Skripte zusammen mit ihrem Status angezeigt wird. In dieser GUI können Skripte ausgeführt, übersprungen und allgemein verwaltet werden.

Zusätzlich kannst du ein `IProcessStartEventBean` definieren, um bei Start deiner Anwendung die benötigten (noch nicht ausgeführten) SQL-Skripte automatisch in der richtigen Reihenfolge auszuführen. Dieses `IProcessStartEventBean` kann einfach erstellt werden, indem du die `AbstractDbUtilsStartEventBean` erweiterst. Beachte, dass dieses Bean im Kontext deiner Anwendung (oder projektabhängig) definiert werden muss, da es Zugriff auf den Classpath deiner Projekte haben muss.

### SQL-Abfragen

Db-Utils bietet eine einfache GUI zum Ausführen von SQL-Skripten. Beachte, dass diese Skripte "wie sie sind" ohne Überprüfung ausgeführt werden und mit den Berechtigungen des für deine Datenbank konfigurierten Benutzers. Die GUI zeigt die Ergebnisse in einem einfachen Textfenster an. Sie ist für schnelle kleine Abfragen oder Online-Korrekturen gedacht und kann mit einem echten Datenbanktool nicht verglichen werden.

### Excel-Export und -Import

Db-Utils bietet eine Export- und Importfunktion für Excel-Dateien und sogar binäre BLOBS. Diese Funktion wird von [DbUnit](https://www.dbunit.org/) bereitgestellt.

**Der Export von Daten** kann auf zwei Arten durchgeführt werden:
* *Excel-Export*: Exportiert eine Excel-Datei mit einem Blatt pro Tabelle.
* *ZIP-Export*: Exportiert eine Excel-Datei mit einem Blatt pro Tabelle, zusätzlich werden alle Spalten, die ein binäres großes Objekt (BLOB) darstellen, in eine eigene Datei exportiert. Die Excel-Datei und alle exportierten Dateien werden in einer ZIP-Datei gespeichert. In der ZIP-Datei werden die BLOB-Spaltendateien in Unterordnern mit dem Namensschema `lob/<TABELLE>/<SPALTE>/file.ext` abgelegt.

**Der Import von Daten** kann mit oder ohne vorherige Bereinigung der Datenbank erfolgen. Beachte, dass dies eine potenziell gefährliche Operation ist, da das Löschen von Einträgen nicht rückgängig gemacht werden kann. Der Datenimport sollte vermutlich nur während Tests verwendet werden, um die Datenbank in einen definierten Testzustand zu versetzen, oder für die Erstkonfiguration der Projekt-Datenbank auf einem neuen Rechner.
* *Excel laden*: Lädt eine Excel-Datei im gleichen Format, das der Export erzeugt.
* *Excel laden und Classpath-Blobs handhaben*: Derzeit kann eine zuvor exportierte ZIP-Datei nicht importiert werden, aber es gibt eine Lösung, die sich in der Projektentwicklung als nützlich erwiesen hat. Der Import lädt eine Excel-Datei im gleichen Format wie der ZIP-Export, verarbeitet jedoch Classpath-Referenzen in den Excel-Spalten. Wenn eine Spalte eine Classpath-Referenz (`classpath:/path`) enthält, wird die Datei in den für DB-Utils definierten Datenressourcen gesucht und als Blob eingefügt. Es wird empfohlen, die BLOB-Dateien in einem Unterordner des `src`-Ordners deines Projekts abzulegen (z.B. `src/data`). Dabei wird angenommen, dass du nur wenige sich selten ändernde BLOB-Testdateien in deinem Projekt hast und keine ZIP-Dateien für jede Spaltenänderung in der importierten Excel-Datei während der Entwicklung erstellen möchtest.

Um ein Beispiel für in deinem Projekt gespeicherte Ressourcen zu sehen, schaue dir bitte den `src/resources`-Ordner des Demo-Projekts an und vergleiche die Einstellungen mit den globalen Variablen (oder dem `DbUtilsResolver` für den Microsoft SQL Server-Teil).

Beachte, dass beim Import die Blätter in deiner Excel-Datei in der richtigen Reihenfolge sein müssen, um keine Einschränkungen zu verletzen. Um die richtige Reihenfolge zu erhalten, ist es am besten, die Datenbank zuerst zu exportieren. Der Export erstellt eine Excel-Datei mit der richtigen Blattreihenfolge.

Beachte, dass Excel Einschränkungen hinsichtlich der maximalen Größe von Spalten und Blättern hat. Diese Funktion kann für Tests oder für die initiale Datenbankeinrichtung nützlich sein, sollte jedoch nicht für Datenbanksicherungen oder ähnliche „ernste“ Datenbankaufgaben verwendet werden.

### Einstellungen

Die Einstellungsseite zeigt einige grundlegende Datenbankeinstellungen, um herauszufinden, welche Datenbank verwendet wird. Sie kann später noch erweitert werden.

### Unterstützung für mehrere Datenbanken

Mehrere Datenbanken werden unterstützt. Jede Datenbank benötigt einen eigenen Resolver und eine eigene Prozessstart-Event-Bean. Natürlich kann der Standardmechanismus zum Abrufen der Konfiguration aus globalen Variablen nur für eine Datenbank verwendet werden. Wenn Sie mehrere Resolver bereitstellen, implementieren Sie auch den Umgang mit verschiedenen Konfigurationen (durch Verwendung von Konstanten, globalen Variablen...).

## Demo

**Hinweis: Das Demo-Projekt muss entpackt werden, damit es im Axon Ivy Designer ausgeführt werden kann.

![Entpacken der Demo](images/unpack.png)

Die Demo zeigt, wie Sie Db-Utils in Ihr Projekt integrieren können. Sie enthält die einfachste mögliche Konfiguration für eine HSQLDB und eine etwas aufwändigere Konfiguration für eine Microsoft SQL Server Datenbank. Der HSQLDB-Teil ist ohne zusätzliche Konfiguration sofort einsatzbereit. Für den Microsoft SQL Server-Teil müssen Sie Zugriff auf eine Microsoft SQL Server-Datenbank haben und deren Anmeldeinformationen konfigurieren. Beachten Sie, dass Db-Utils bedingungslos SQL-Anweisungen gegen diese Datenbank ausführen wird!

### Db-Utils GUI

Der größte Teil der Demo kann in der Db-Utils GUI betrachtet werden. Die GUI hat Registerkarten für verschiedene Operationen und einen gemeinsamen Nachrichtenbereich zur Anzeige der Ergebnisse. Um die GUI zu benutzen, verwenden Sie den Benutzer `dbadmin` oder erstellen Sie einen Benutzer mit der Rolle `DbUtilsAdministrator`.

### Inkrementelle Updates

Die Registerkarte **Incremental Updates** zeigt eine Übersicht der verfügbaren SQL-Skripte und eine Übersicht der Skripte, die einmal vorhanden waren, aber nicht mehr als Ressourcen verfügbar sind. Für jedes Skript werden das Ausführungsdatum, Fehler und ein Status angezeigt. Skripte können manuell ausgeführt, aktualisiert, deaktiviert, ignoriert oder gelöscht werden (nur für nicht verfügbare Skripte verfügbar).

Es stehen Verknüpfungen zur Verfügung, um alle Skripte auszuführen, die noch nicht erfolgreich ausgeführt wurden, und um im Falle von Fehlern die Fortsetzung zu erzwingen.

![Inkrementelle Aktualisierungen](images/incremental.png)

### SQL-Anweisungen

Auf der Registerkarte **SQL Statements** können Sie einfache SQL-Anweisungen für die Datenbank ausführen. Die Ergebnisse werden im Nachrichtenbereich angezeigt.

![SQL-Anweisungen](images/sql.png)

### Excel-Export/Import

Die Registerkarte **Excel Export/Import** dient zum Exportieren der gesamten Datenbank in eine Excel- oder ZIP-Datei oder zum Importieren der gesamten Datenbank (oder von Teilen davon) aus einer Excel-Datei. Beim Importieren können Sie auswählen, dass die Datenbank vor dem Import bereinigt werden soll. Hinweis: Diese Bereinigung bereinigt alle Tabellen, die in der importierten Excel-Datei erwähnt werden, bedingungslos. Es ist möglich, inkrementell zu importieren, wenn Sie keine Datenbankbeschränkungen verletzen.

Die Demo-Db-Utils-Skripte erstellen drei Demo-Tabellen und füllen sie mit Daten. Um die Export/Import-Funktionalität auszuprobieren, können Sie die vorhandenen Daten in eine Excel-Datei exportieren, dann auf die Registerkarte SQL-Anweisungen gehen und alle Daten in den drei Demo-Tabellen mit den unten gezeigten Anweisungen löschen und anschließend die vorherige Excel-Datei wieder importieren. Danach sollten die Daten wieder vorhanden sein.

```
delete from logo;
delete from hero;;
delete from brand;
```

Zwei Excel-Dateien für Tests sind vorhanden.

* `export-with-blobs.xls` Eine Excel-Datei, die Daten für alle Tabellen und Binärdaten eines Blobs direkt enthält
* `export-with-blobs-from-classpath.xls` Eine Excel-Datei, die Daten für alle Tabellen enthält, aber Binärdaten aus den Projektressourcen (Klassenpfad) referenziert

![Excel-Export und -Import](images/eximport.png)


### Settings

Die Registerkarte **Settings** zeigt die aktuellen Einstellungen, die von Db-Utils verwendet werden.

![Einstellungen](images/settings.png)

## Setup

Um Db-Utils in dein Projekt zu integrieren und zu verwenden, musst du (für jede Datenbank, die du unterstützen möchtest)
* eine projektlokale `DBUtilsResolver`-Klasse bereitstellen
* eine projektlokale `DbUtilsStartEventBean`-Klasse bereitstellen
* einen Startprozess erstellen, der die Db-Utils GUI aufruft
* einen Programmstart erstellen, der das `DbUtilsStartEventBean` verwendet
* die Konfiguration überprüfen
* die Sicherheit überprüfen

Im Demoprojekt findest du Beispiele für eine einfache Einrichtung (HSQLDB-Teil) und eine etwas komplexere, angepasste Einrichtung (Microsoft SQL Server-Teil). Bitte vergleiche die folgende Beschreibung mit diesen Beispielen.

### Bereitstellung von `DbUtilsResolver`

Der DbUtilsResolver wird verwendet, um alle Konfigurationsinformationen für eine der in deinem Projekt definierten Datenbanken zu verwalten (z. B. Name, Ressourcenpfade, DBUtilsScript-Tabellendefinition usw.). Es ist wichtig, dass diese Klasse in einem Projekt implementiert wird, das entweder deine Datenbank und Skriptressourcen definiert oder eine Abhängigkeit zu einem solchen Projekt hat. Sie kann durch Erweitern der von DB-Utils bereitgestellten Klasse `AbstractDbUtilsResolver` implementiert werden. Implementierungen für Microsoft SQL Server (`MSSQL2005DbUtilsResolver`) und HSQLDB (`HSQLDbUtilsResolver`) werden direkt von DB-Utils bereitgestellt.

### DbUtilsStartEventBean bereitstellen

Das `DbUtilsStartEventBean` wird als Java-Klasse in einem Event-Prozess-Start verwendet. Es sollte die von DB-Utils bereitgestellte Klasse `AbstractDbUtilsStartEventBean` erweitern und einen Standardkonstruktor implementieren, der den projektspezifischen `DbUtilsResolver` setzt.

### Db-Utils GUI Prozessstart erstellen

Erstelle einen Startprozess, der die Db-Utils GUI (und den projektspezifischen `DbUtilsResolver`) verwendet, die vom Db-Utils-Projekt bereitgestellt wird (siehe unten). Beachte, dass dieser Start durch eine autorisierte Rolle des Projekts gesichert werden sollte!

![GUI Integration](images/gui.png)

### Programmstart erstellen

Erstelle einen Programmstart, der das projektspezifische `DbUtilsStartEventBean` verwendet (siehe unten).

![Process Start Event Bean](images/starteventbean.png)

### Konfiguration

Klassen, die `AbstractDbUtilsResolver` erweitern, können über globale Variablen konfiguriert werden. Die wichtigsten globalen Variablen (Einstellungen) sind:
* Name der Datenbank, wie in der Ivy-Datenbankkonfiguration definiert.
* Die Skript-URL, um inkrementelle SQL-Skripte zu finden. Diese Skripte können im Dateisystem liegen, aber eine bequemere Methode ist es, sie als Ressource in dein Projekt zu legen, indem du das Classpath-Schema in der URL verwendest. Auf diese Weise werden sie automatisch bereitgestellt und sind immer auf dem neuesten Stand mit deinem Projekt.
* Die Daten-URL, die für andere Daten verwendet wird, z. B. für Binärdateien, die in Excel-BLOB-Imports verwendet werden können.
* Zusätzliche Einstellungen, um automatische Updates zu konfigurieren und GUI-Registerkarten ein- oder auszuschalten.

Bitte prüfe das Demoprojekt, um den verwendeten Classpath-Mechanismus für SQL-Skripte und Blob-Dateien besser zu verstehen.

```
@variables.yaml@
```

### Sicherheit

Db-Utils kann verwendet werden, um beliebige SQL-Skripte ohne weitere Prüfungen direkt auf die konfigurierte Datenbank mit den Berechtigungen des konfigurierten Benutzers auszuführen. Es ist daher wichtig, den Db-Utils-GUI-Start mit einer erhöhten Rolle in deinem Projekt (`DbUtilsAdmin` oder ähnlich) zu sichern. Außerdem ist es möglich, jede Registerkarte (Funktionalität) in der Db-Utils GUI per Konfiguration zu deaktivieren.

Um die automatische Update-Funktion von Db-Utils zu verwenden, benötigt der konfigurierte Datenbankbenutzer höchstwahrscheinlich erweiterte Datenbankberechtigungen (z. B. zum Ändern von Tabellendefinitionen), die du möglicherweise nicht in deiner Anwendung haben möchtest.

In diesem Fall könnte es eine Idee sein, ein separates Projekt (z. B. ein Tools-Projekt) zu erstellen, das von deinen Projekten abhängt und alle Db-Utils-spezifischen Implementierungen und eine spezielle, erweiterte Datenbankkonfiguration in diesem separaten Projekt unterbringt.



