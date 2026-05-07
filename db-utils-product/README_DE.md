# DB-Utils

Db-Utils ist eine Sammlung von Werkzeugen, die dir bei typischen Datenbankaufgaben in deinem Axon Ivy‑Projekt helfen. Es bietet sichere, geführte UI‑Abläufe, mit denen du inkrementelle SQL‑Skripte ausführen, Migrationen durchführen sowie Daten importieren und exportieren kannst. So kann dein Team Projektdatenbanken zuverlässig verwalten.

### Wichtigste Funktionen

- Führe und verwalte Datenbank‑Skripte direkt über eine einfach zu bedienende Oberfläche.
- Nutze Liquibase für zuverlässige, versionierte Datenbankmigrationen.
- Führe Ad‑hoc‑SQL‑Abfragen aus und sieh dir die Ergebnisse direkt in der Oberfläche an.
- Importiere und exportiere Daten per Excel für schnelle Massenaktualisierungen.
- Führe inkrementelle Updates über geführte Workflows aus, um Risiken zu minimieren.
- Zentralisiere Datenbank‑Einstellungen und Steuerung für eine einheitliche Nutzung im Team.

## Konzepte

Die wichtigste Funktion von DB‑Utils ist die automatische Aktualisierung deiner Datenbank bei jeder Bereitstellung. Außerdem kannst du Daten einfach als Excel‑ oder ZIP‑Dateien exportieren und importieren sowie einfache Abfragen direkt über die Db‑Utils‑GUI in deiner Anwendung ausführen. Indem du einen Resolver bereitstellst, einige Einstellungen über globale Variablen vornimmst und ggf. ein Process‑Start‑Event‑Bean anlegst, kannst du alle Funktionen von DB‑Utils nutzen.

### Inkrementelle Updates

Db‑Utils verwaltet eine Liste inkrementeller SQL‑Skripte und deren Ausführungsstatus für dein Projekt und die zugehörige Datenbank. Wenn Db‑Utils zum ersten Mal ausgeführt wird (entweder über die GUI oder automatisch beim Programmstart), legt es eine Tabelle an, die diese Liste enthält. Der Tabellenname kann in deinem `DbUtilsResolver` überschrieben werden; standardmäßig heißt er `DbUtilsScripts`. Skripte kannst du manuell über die Db‑Utils‑GUI ausführen oder sie automatisch beim Start ausführen lassen. Die SQL‑Skripte können im Dateisystem liegen oder in einem Ressourcen‑(Classpath‑)Verzeichnis (empfohlen). Als Konvention werden die Skripte alphabetisch nach Dateiname sortiert, angezeigt und ausgeführt. Es wird empfohlen, die inkrementellen Dateien in den Classpath deines Projekts zu legen, z. B. in einen Unterordner von `src` (z. B. `src/resources/sql/incremental`) und ein konsistentes Namensschema zu verwenden.

`YYYYMMDD-HHMM-Ticket-Short-Description.sql`

Db‑Utils legt eine Tabelle an, in der vermerkt ist, welche dieser SQL‑Skripte ausgeführt wurden, und stellt eine GUI zur Anzeige und Pflege dieses Skriptstatus zur Verfügung. Skripte können ausgeführt, übersprungen und verwaltet werden.

Außerdem kannst du ein `IProcessStartEventBean` definieren, um benötigte (noch nicht ausgeführte) SQL‑Skripte automatisch und in der richtigen Reihenfolge beim Start deiner Anwendung auszuführen. Dieses `IProcessStartEventBean` lässt sich einfach erstellen, indem du `AbstractDbUtilsStartEventBean` erweiterst. Beachte, dass dieses Bean im Kontext deiner Anwendung definiert sein muss (oder von Projekten abhängen muss), damit es Zugriff auf den Classpath hat.

Beachte außerdem, dass es einen alternativen Mechanismus für Datenbank‑Updates gibt, der auf [Liquibase](https://liquibase.com) basiert.

### Liquibase — Inkrementelle Aktualisierungen

Eine alternative Aktualisierungslösung basiert auf [Liquibase](https://liquibase.com). Lege einfach eine Changelog‑Datei in deinem `DbUtilsResolver` fest und implementiere bei Bedarf ein StartEvent‑Bean, um automatische Updates beim Anwendungsstart durchzuführen. Weitere Informationen zu Liquibase findest du in der offiziellen Dokumentation.

### SQL‑Abfragen

Db‑Utils stellt dir eine einfache GUI zum Ausführen von SQL‑Skripten bereit. Diese Skripte werden „as‑is“ ohne zusätzliche Prüfungen und mit den Rechten des konfigurierten Datenbank‑Benutzers ausgeführt. Die GUI zeigt die Ergebnisse in einem einfachen Textfenster an. Sie ist für schnelle Abfragen oder kleinere Online‑Korrekturen gedacht und ersetzt kein vollwertiges Datenbank‑Werkzeug.

### Excel‑Export und -Import

Db‑Utils bietet eine Export‑ und Importfunktion für Excel‑Dateien und sogar binäre BLOBs. Diese Funktion wird durch [DbUnit](https://www.dbunit.org/) realisiert.

**Datenexport** ist auf zwei Wegen möglich:
* *Export Excel* — Erzeuge eine Excel‑Datei mit einem Sheet pro Tabelle.
* *Export ZIP* — Erzeuge eine Excel‑Datei mit einem Sheet pro Tabelle und exportiere zusätzlich alle Spalten, die BLOBs enthalten, in eigene Dateien. Die Excel‑Datei und alle exportierten Dateien werden in einer ZIP‑Datei gespeichert. In der ZIP‑Datei werden BLOB‑Dateien unter dem Namensschema `lob/<TABLE>/<COLUMN>/file.ext` abgelegt.

**Datenimport** kann mit oder ohne vorherige Bereinigung der Datenbank erfolgen. Beachte, dass dieser Vorgang Daten löschen kann und nicht rückgängig gemacht werden kann. Importiere Daten vorzugsweise in Testumgebungen oder zur Ersteinrichtung einer Projekt‑Datenbank.
* *Load Excel* — Lade eine Excel‑Datei im gleichen Format, das der Export erzeugt.
* *Load Excel and handle classpath blobs* — Eine zuvor exportierte ZIP‑Datei lässt sich derzeit nicht direkt importieren; es gibt jedoch eine nützliche Lösung für Entwicklungsfälle. Der Import verarbeitet Classpath‑Referenzen in Excel‑Spalten: Enthält eine Spalte eine Classpath‑Referenz (`classpath:/path`), wird die Datei in den konfigurierten Datenressourcen gesucht und als Blob eingefügt. Es empfiehlt sich, BLOB‑Dateien in einen Unterordner von `src` zu legen (z. B. `src/data`). Die Annahme ist, dass du nur wenige, selten geänderte BLOB‑Testdateien im Projekt hast und daher nicht für jede Änderung ZIP‑Dateien erzeugen möchtest.

Um ein Beispiel für Projektressourcen zu sehen, untersuche den Demo‑Projektordner `src/resources` und vergleiche die Einstellungen mit den globalen Variablen (oder deinem `DbUtilsResolver` für den Microsoft SQL‑Server‑Teil).

Beachte, dass die Sheets beim Import in der richtigen Reihenfolge vorliegen müssen, um Constraint‑Fehler zu vermeiden. Der Export erzeugt die richtige Reihenfolge.

Beachte außerdem, dass Excel Beschränkungen bei Spalten‑ und Sheet‑Größen hat. Die Funktion eignet sich für Tests oder die Erstbefüllung, ist jedoch nicht als Backup‑Lösung gedacht.

## Demo

![Demo entpacken](images/unpack.png)

Die Demo zeigt, wie du Db‑Utils in dein Projekt integrierst und enthält sofort lauffähige Beispiele für HSQLDB und Microsoft SQL Server.

### DbUtils HSQLDB
So startest du
- Starte die Anfrage mit dem Namen "DbUtils HSQLDB" über die Start-/Anfragen‑Seite der Anwendung.

Was du siehst & Schritte
1. Das Db‑Utils‑Administrationsfenster öffnet sich, vorkonfiguriert für die HSQLDB‑Demoinstanz.
2. Die Hauptansicht zeigt eine Übersicht über die Demo‑Datenbank und bietet Bedienelemente für übliche DB‑Aufgaben.
3. Wenn du eine Aktion auswählst, wirst du um Bestätigung gebeten; die UI zeigt anschließend den Fortschritt sowie eine eindeutige Erfolgs‑/Fehlermeldung an.

Aktionen, die du durchführen kannst
- Tabellen und Beispieldaten ansehen.
- Datenbank‑Updates oder Migrationen durchführen.
- Vorhandene Demodaten anwenden oder zurücksetzen.
- Ad‑hoc‑SQL‑Abfragen oder Wartungsaufgaben ausführen und Ergebnisse/Logs aktualisieren.

Wer es starten kann
- Zum Starten dieser Demo benötigst du die Rolle `DbUtilsAdministrator`.

Hinweise
- Die Demo enthält außerdem Autostart‑Einträge (DbUtils Autostart HSQLDB und Liquibase Autostart HSQLDB), die beim Anwendungsstart ausgeführt werden können, um die Demo‑Datenbank vorzubereiten oder zu aktualisieren.

---

### DbUtils MSSQL
So startest du
- Starte die Anfrage mit dem Namen "DbUtils MSSQL" über die Start-/Anfragen‑Seite der Anwendung.

Was du siehst & Schritte
1. Dasselbe Db‑Utils‑Administrationsfenster öffnet sich, vorkonfiguriert für die MSSQL‑Demoinstanz.
2. Die Hauptansicht zeigt MSSQL‑spezifische Verbindungen und die gleichen DB‑Bedienelemente.
3. Wähle eine Aktion, bestätige die Ausführung, und beobachte den Fortschritt sowie die Abschlussmeldung.

Aktionen, die du durchführen kannst
- Tabellen und Beispieldaten ansehen.
- Datenbank‑Updates oder Migrationen durchführen.
- Demodaten verwalten (anwenden/zurücksetzen).
- Ad‑hoc‑SQL‑Abfragen oder Wartungsaufgaben ausführen und Ergebnisse/Logs prüfen.

Wer es starten kann
- Zum Starten dieser Demo benötigst du die Rolle `DbUtilsAdministrator`.

Hinweise
- Die Demo enthält Autostart‑Einträge (DbUtils Autostart MSSQL und Liquibase Autostart MSSQL), die beim Anwendungsstart ausgeführt werden können, um die MSSQL‑Demo‑Datenbank vorzubereiten oder zu aktualisieren.

Allgemein
- Beide Anfragen öffnen denselben Db‑Utils‑Dialog; jede Anfrage ist jedoch für die Ziel‑Datenbank (HSQLDB oder MSSQL) vorkonfiguriert, sodass die UI und die verfügbaren Operationen zur gewählten Demo passen.

### Db‑Utils GUI

![Anwendungsoberfläche (Hauptübersicht)](images/gui.png)

Die meisten Demo‑Funktionen findest du in der Db‑Utils‑GUI. Die GUI hat Tabs für verschiedene Operationen und einen gemeinsamen Nachrichtenbereich zur Anzeige von Ergebnissen. Zum Nutzen der GUI meldest du dich als `dbadmin` an oder erstellst einen Benutzer mit der Rolle `DbUtilsAdministrator`.

### Inkrementelle Updates

Der Tab **Inkrementelle Updates** zeigt eine Übersicht über verfügbare SQL‑Skripte und eine Liste von Skripten, die nicht mehr als Ressourcen vorhanden sind. Für jedes Skript werden Ausführungsdatum, Fehler und Status angezeigt. Skripte können manuell ausgeführt, aktualisiert, deaktiviert, ignoriert oder gelöscht werden.

![Fortschritt inkrementeller Importe und Diff‑Ansicht](images/incremental.png)

### Liquibase — Inkrementelle Aktualisierungen

Der Tab **Liquibase** bietet einen Button, um das Liquibase‑Update zu starten. Fehler werden auf der Seite angezeigt.

![Beispiel für Liquibase‑Changelog und Migrationsübersicht](images/liquibase.png)

### SQL‑Statements

Der Tab **SQL‑Statements** dient zum Ausführen einfacher SQL‑Statements gegen die Datenbank. Ergebnisse werden im Nachrichtenbereich angezeigt.

![SQL‑Editor oder Beispiel‑SQL‑Skript, das von Db‑Utils verwendet wird](images/sql.png)

### Excel‑Export/Import

Der Tab **Excel‑Export/Import** dient dazu, die gesamte Datenbank in eine Excel‑ oder ZIP‑Datei zu exportieren oder (Teil‑)Daten aus einer Excel‑Datei zu importieren. Beim Import kannst du wählen, ob die Datenbank zuvor bereinigt werden soll. Diese Bereinigung löscht alle in der importierten Excel genannten Tabellen bedingungslos. Ein inkrementeller Import ist möglich, wenn du keine Datenbank‑Constraints verletzt.

![Export-/Import‑Dialog mit Importergebnissen](images/eximport.png)

### Einstellungen

Der Tab **Einstellungen** zeigt die aktuellen Konfigurationseinstellungen von Db‑Utils.

![Einstellungsdialog mit verfügbaren Konfigurationsoptionen](images/settings.png)

## Einrichtung

Um DB‑Utils in deinem Projekt zu integrieren und zu nutzen, musst du (für jede Datenbank, die du unterstützen möchtest)
- eine projekt‑lokale `DbUtilsResolver`‑Klasse bereitstellen
- eine projekt‑lokale `DbUtilsStartEventBean`‑Klasse bereitstellen
- einen Start‑Prozess erstellen, der die Db‑Utils‑GUI aufruft
- einen Programmstart anlegen, der das `DbUtilsStartEventBean` verwendet
- die Konfiguration prüfen
- die Sicherheit prüfen

### `DbUtilsResolver` bereitstellen

Der `DbUtilsResolver` enthält die Konfigurationsinformationen für eine Datenbank (Name, Ressourcenpfade, Changelog, Skripte usw.). Du kannst `AbstractDbUtilsResolver` erweitern oder eine eigene Implementierung bereitstellen.

### `DbUtilsStartEventBean` und/oder `LiquibaseStartEventBean` bereitstellen

Das `DbUtilsStartEventBean` und/oder `LiquibaseStartEventBean` kann verwendet werden, um automatische Updates beim Anwendungsstart auszuführen, indem du `AbstractDbUtilsStartEventBean` erweiterst und einen Projekt‑Resolver bereitstellst.

### Db‑Utils GUI Startprozess erstellen

Erstelle einen Start‑Prozess, der die Db‑Utils‑GUI (und den Projekt‑`DbUtilsResolver`) verwendet. Schütze diesen Start mit einer erhöhten Rolle in deinem Projekt.

![GUI‑Integration](images/gui.png)

### Programmstart erstellen

Erstelle einen Programmstart, der das `DbUtilsStartEventBean` deines Projekts verwendet.

![StartEventBean‑Konfigurationsdiagramm oder Codebeispiel](images/starteventbean.png)

### Konfiguration

Klassen, die `AbstractDbUtilsResolver` erweitern, lassen sich über globale Variablen konfigurieren. Zu den wichtigsten Einstellungen gehören Name der Datenbank, Scripts‑URL, Data‑URL (für BLOBs) sowie Feature‑Schalter für Tabs und Autoupdate.

```
@variables.yaml@
```

### Sicherheit

Db‑Utils kann SQL direkt gegen die konfigurierte Datenbank ausführen. Sichere die Db‑Utils‑GUI mit einer administrativen Rolle (z. B. `DbUtilsAdministrator`) und vermeide es, mächtige Zugangsdaten in Produktionsumgebungen offenzulegen.

## Komponenten

### Exponierte CALLABLE_SUB‑Prozesse

Keine CALLABLE_SUB‑Prozessdateien gefunden.

### Formular‑Komponenten

#### Dialoge

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/SqlStatement.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/Settings.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/DbUtils.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/IncrementalUpdates.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/ExcelExportImport.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### DbUtilsData

- **Namensraum**: com.axonivy.utils.db.DbUtils
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/DbUtils/Liquibase.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.DbUtilsController)

#### Komponenten

#### ScriptTableData

- **Namensraum**: com.axonivy.utils.db.ScriptTable
- **Pfade**:
  - xhtml: db-utils/src_hd/com/axonivy/utils/db/ScriptTable/ScriptTable.xhtml
- **Komponententyp**: HTML_DIALOG
- **Parameter**:
  - ctrl (com.axonivy.utils.db.controller.ScriptTableController)

---

### Open API‑Ressourcen

- Für dieses Produkt sind keine öffentlichen OpenAPI‑Spezifikationen verfügbar

### Maven‑Artefakte

1. db-utils

```xml
<dependency>
  <groupId>com.axonivy.utils.db</groupId>
  <artifactId>db-utils</artifactId>
  <type>iar</type>
</dependency>
```
2. db-utils-demo

```xml
<dependency>
  <groupId>com.axonivy.utils.db</groupId>
  <artifactId>db-utils-demo</artifactId>
  <type>iar</type>
</dependency>
```
