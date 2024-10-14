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

Db-Utils arbeitet, indem es eine Liste inkrementeller SQL-Skripte sowie deren Ausführungsstatus zusammen mit deinem Projekt und der zugehörigen Datenbank verwaltet. Wenn Db-Utils zum ersten Mal ausgeführt wird (entweder über die GUI oder automatisch beim Start deiner Anwendung), wird eine Tabelle erstellt, um diese Liste zu verwalten. Der Tabellenname lässt sich in deinem `DbUtilsResolver` anpassen, wobei der Standardname `DbUtilsScripts` lautet. Die SQL-Skripte können manuell über die Db-Utils-GUI oder automatisch bei jedem Start deiner Anwendung ausgeführt werden. Sie können entweder in einem Dateisystemordner oder in einem Ressourcenverzeichnis (Classpath) gespeichert werden – letzteres ist die bevorzugte Methode.

Es wird empfohlen, die inkrementellen Dateien deines Projekts im Classpath deines Projekts abzulegen, zum Beispiel in einem Unterordner des `src`-Verzeichnisses (z.B. `src/resources/sql/incremental`). Außerdem solltest du ein einheitliches Namensschema für deine Skripte verwenden, wie z.B. 

`YYYYMMDD-HHMM-Ticket-Short-Description.sql`

Db-Utils erstellt eine Tabelle, in der gespeichert wird, welche dieser SQL-Skripte ausgeführt wurden, und bietet eine grafische Benutzeroberfläche, in der die Liste der Skripte zusammen mit ihrem Status angezeigt wird. In dieser GUI können Skripte ausgeführt, übersprungen und allgemein verwaltet werden.

Zusätzlich können Sie eine `IProcessStartEventBean` definieren, um benötigte (noch nicht ausgeführte) SQL-Skripte automatisch in der richtigen Reihenfolge beim Start Ihrer Anwendung auszuführen. Diese `IProcessStartEventBean` kann einfach durch Erweiterung von `AbstractDbUtilsStartEventBean` erstellt werden. Beachten Sie, dass diese Bean im Kontext Ihrer Anwendung definiert werden muss (oder von Ihren Projekten abhängt), da sie Zugriff auf den Klassenpfad Ihrer Projekte haben muss.

### SQL Abfragen

Db-Utils bietet eine einfache GUI zur Ausführung von SQL-Skripten. Beachten Sie, dass diese Skripte ohne jegliche Prüfung und mit den Rechten des für Ihre Datenbank konfigurierten Benutzers ausgeführt werden. Die GUI zeigt die Ergebnisse in einem einfachen Textfenster an. Sie ist für schnelle kleine Nachforschungen oder Online-Korrekturen gedacht und nicht mit einem echten Datenbankwerkzeug zu vergleichen.

### Excel-Export und -Import

Db-Utils bietet eine Export- und Importfunktion für Excel-Dateien und sogar binäre BLOBS. Diese Funktion wird von [DbUnit](https://www.dbunit.org/) implementiert.

Der **Export von Daten** kann auf zwei Arten erfolgen:
* *Export Excel* Export einer Excel-Datei mit einem Blatt pro Tabelle
* *Export ZIP* Export einer Excel-Datei mit einem Blatt pro Tabelle, aber zusätzlich Export aller Spalten, die ein binäres großes Objekt (BLOB) darstellen, in eine eigene Datei. Das Excel und alle exportierten Dateien werden in einer ZIP-Datei gespeichert. In der ZIP-Datei werden die BLOB-Spalten-Dateien in Unterordnern mit der Namenskonvention `lob/<TABLE>/<COLUMN>/file.ext` abgelegt.

Der **Datenimport** kann mit oder ohne vorherige Bereinigung der Datenbank erfolgen. Beachten Sie, dass dies eine potentiell gefährliche Operation ist, da das Löschen von Einträgen nicht rückgängig gemacht werden kann. Das Importieren von Daten sollte wahrscheinlich nur während Tests verwendet werden, um eine Datenbank in einen definierten Testzustand zu versetzen oder für eine Ersteinrichtung Ihrer Projektdatenbank auf einem neuen Rechner.
* *Excel laden* Laden Sie ein Excel im gleichen Format wie der Export erzeugt.
* *Excel laden und Klassenpfad-Blobs behandeln* Derzeit kann eine zuvor exportierte ZIP-Datei nicht importiert werden, aber es wird eine Lösung angeboten, die sich bei Projektentwicklungen als nützlich erwiesen hat. Der Import lädt eine Excel-Datei im gleichen Format wie der ZIP-Export, behandelt aber Klassenpfad-Referenzen in Excel-Spalten. Wenn eine Spalte eine Klassenpfadreferenz (`classpath:/path`) enthält, wird die Datei in den für DB-Utils definierten Datenressourcen nachgeschlagen und die Datei als Blob eingefügt. Es wird empfohlen, die BLOB-Dateien in einem Unterordner des src-Ordners Ihres Projekts abzulegen (z.B. `src/data`). Die Annahme ist, dass Sie nur einige wenige, sich selten ändernde BLOB-Testdateien in Ihrem Projekt zum Testen haben werden und nicht für jede Spaltenänderung im importierten Excel während der Entwicklung ZIP-Dateien erstellen wollen.

Um ein Beispiel für die in Ihrem Projekt gespeicherten Ressourcen zu sehen, untersuchen Sie bitte den Ordner `src/resources` des Demoprojekts und vergleichen Sie die Einstellungen in den globalen Variablen (oder `DbUtilsResolver` für den Microsoft SQL Server Teil).

Beachten Sie, dass beim Importieren die Blätter in Excel in der richtigen Reihenfolge sein müssen, damit keine Einschränkungen verletzt werden. Um die richtige Reihenfolge zu erhalten, ist es am besten, die Datenbank zuerst zu exportieren. Durch den Export wird eine Excel-Datei mit der richtigen Blattreihenfolge erstellt.

Beachten Sie, dass Excel Beschränkungen hinsichtlich der maximalen Größe von Spalten und Blättern hat. Diese Funktion kann für Tests oder die Ersteinrichtung von Datenbanken hilfreich sein, sollte aber nicht für Datenbanksicherungen und ähnliche „ernsthafte“ Datenbankaufgaben verwendet werden.

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

## Einstellungen

Um DB-Utils in Ihr Projekt zu integrieren und zu nutzen, müssen Sie (für jede Datenbank, die Sie unterstützen wollen)
* eine projektlokale Klasse `DBUtilsResolver` bereitstellen
* eine projektlokale Klasse `DbUtilsStartEventBean` bereitstellen
* eine Startprozess erstellen, der die DB-Utils GUI aufruft
* einen Programmstart erstellen unter Verwendung der `DbUtilsStartEventBean` Klasse
* die Konfiguration prüfen
* die Sicherheit prüfen

Im Demoprojekt finden Sie Beispiele für ein einfaches Setup (HSQLDB Teil) und ein etwas komplexeres, angepasstes Setup (Microsoft SQL Server Teil). Bitte vergleichen Sie die folgende Beschreibung mit diesen Beispielen.

### Bereitstellung von `DbUtilsResolver`

Der DbUtilsResolver wird verwendet, um alle Konfigurationsinformationen für eine der in Ihrem Projekt definierten Datenbanken zu speichern (z.B. Name, Ressourcenpfade, DBUtilsScript Tabellendefinition...). Es ist wichtig, dass diese Klasse in einem Projekt implementiert wird, das entweder ein Projekt definiert oder eine Abhängigkeit zu einem Projekt hat, das Ihre Datenbank- und Skriptressourcen definiert. Sie kann durch Erweiterung der Klasse `AbstractDbUtilsResolver`, die von DB-Utils bereitgestellt wird, implementiert werden. Implementierungen für Microsoft SQL Server (`MSSQL2005DbUtilsResolver`) und HSQLDB (`HSQLDbUtilsResolver`) werden direkt von DB-Utils bereitgestellt.

### DbUtilsStartEventBean bereitstellen

Die `DbUtilsStartEventBean` wird als Java-Klasse in einem Ereignisprozessstart verwendet. Sie sollte `AbstractDbUtilsStartEventBean`, die von DB-Utils bereitgestellt wird, erweitern und einen Standardkonstruktor implementieren, der den `DbUtilsResolver` des Projekts setzen muss.

### Db-Utils GUI Prozessstart erstellen

Erstellen Sie einen Startprozess, der die Db-Utils GUI (und das Projekt `DbUtilsResolver`) verwendet, die vom Db-Utils Projekt bereitgestellt wird (siehe unten). Beachten Sie, dass Sie diesen Start durch eine autorisierte Rolle des Projekts absichern sollten!

![GUI Integration](images/gui.png)

### Programmstart erstellen

Erstellen Sie einen Programmstart, der das Projekt `DbUtilsStartEventBean` verwendet (siehe unten).

![Process Start Event Bean](images/starteventbean.png)

### Konfiguration

Klassen, die `AbstractDbUtilsResolver` erweitern, können durch globale Variablen konfiguriert werden. Die wichtigsten globalen Variablen (Einstellungen) sind:
* Name der Datenbank, wie in der Ivy-Datenbankkonfiguration definiert.
* Die Skript-URL, um inkrementelle SQL-Skripte zu finden. Diese Skripte können sich im Dateisystem befinden, aber ein bequemerer Weg ist es, sie als Ressource in Ihr Projekt zu integrieren, indem Sie das Klassenpfadschema in der URL verwenden. Auf diese Weise werden sie automatisch bereitgestellt und sind immer auf dem neuesten Stand mit Ihrem Projekt.
* Die Daten-URL wird für andere Daten verwendet, z. B. für Binärdateien, die in Excel-BLOB-Importen verwendet werden können.
* Zusätzliche Einstellungen, um automatische Updates zu konfigurieren und GUI-Registerkarten zu aktivieren oder zu deaktivieren

Bitte sehen Sie sich das Demo-Projekt an, um den Klassenpfad-Mechanismus für SQL-Skripte und Blob-Dateien besser zu verstehen.

```
@variables.yaml@
```

### Sicherheit

Db-Utils können verwendet werden, um beliebige SQL-Skripte ohne weitere Prüfung direkt auf der konfigurierten Datenbank mit den Rechten des konfigurierten Benutzers auszuführen. Es ist daher wichtig, den Start der Db-Utils GUI mit einer besonderen Rolle in Ihrem Projekt abzusichern (`DbUtilsAdmin` oder ähnlich). Zusätzlich ist es möglich, jede Registerkarte (Funktionalität) in Db-Utils GUI durch Konfiguration abzuschalten.

Um die automatische Aktualisierungsfunktion von Db-Utils zu nutzen, benötigt der konfigurierte Datenbankbenutzer höchstwahrscheinlich erweiterte Datenbankrechte (z.B. zum Ändern von Tabellendefinitionen), die Sie in Ihrer Anwendung vielleicht nicht haben wollen.

In diesem Fall könnte es eine Idee sein, ein separates Projekt (z.B. ein Tools-Projekt) in Abhängigkeit von Ihren Projekten zu erstellen und alle Db-Utils-spezifischen Implementierungen und eine spezielle, erweiterte Datenbankkonfiguration in dieses separate Projekt zu legen.



