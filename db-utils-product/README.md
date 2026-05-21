# DB-Utils

Db-Utils is a collection of tools to help with typical database-tasks in your project. It comes with support for automatic, incremental SQL updates of database tables, export and import of data and a simple database query window. Support for Microsoft SQL and HSQLDB is provided out of the box, but it is easy to extend the component for other database types.

Read our [documentation](db-utils-product/README.md).


![GUI screenshot](images/gui.png)

![Integration overview](images/integrate.png)

## Key features

- Keep database schemas up-to-date with automatic, incremental SQL updates, reducing manual migration effort.
- Import and export datasets using Excel for quick data transfer between environments.
- Run ad-hoc SQL queries and inspect results with the built-in query window.
- Support for Microsoft SQL and HSQLDB, with easy extensibility for additional database types.
- Provide a user-friendly admin UI with tabs for incremental updates, Liquibase integration, SQL execution, Excel import/export, and settings.
- Enable automated startup updates and demo workflows for rapid evaluation and deployment.

## Demo

Check the demo implementations provided in the `db-utils-demo` module. They include HSQLDB and MSSQL demos that demonstrate autoupdate, Liquibase integration, and the DbUtils dialog. These demos help you evaluate and integrate the features quickly.


![Autostart example](images/starteventbean.png)

### Demo workflows

#### Db Utils Demo (db-utils-demo)

##### DbUtils HSQLDB

1. Launch the demo from the demo menu or dashboard.
2. You'll see the DbUtils dialog with tabs for Incremental Updates, Liquibase, SQL Statement, Excel Export/Import, and Settings.
3. Use the Incremental Updates or Liquibase tab to apply schema updates, or use the SQL Statement tab to run ad-hoc queries.
4. Review the results and logs shown in the Messages area; autostart can apply updates automatically on startup.

##### DbUtils MSSQL

1. Launch the demo from the demo menu or dashboard.
2. The DbUtils dialog appears with the same tabs (Incremental Updates, Liquibase, SQL Statement, Excel Export/Import, Settings).
3. Perform actions such as running incremental updates, executing SQL statements, or exporting/importing Excel data.
4. Review the results and confirmation messages in the UI.

## Setup

- **Roles:** Everybody (configured in config/roles.xml)

- **OpenAPI:** No public OpenAPI specs delivered by this extension.


 
![Incremental updates](images/incremental.png)

![Liquibase](images/liquibase.png)

![SQL statement](images/sql.png)

![Excel export/import](images/eximport.png)

![Settings](images/settings.png)

![Unpack](images/unpack.png)

### Variables

```yaml
# yaml-language-server: $schema=https://json-schema.axonivy.com/app/12.0.0/variables.json
# == Variables ==
# 
# You can define here your project Variables.
# If you want to define/override a Variable for a specific Environment, 
# add an additional ‘variables.yaml’ file in a subdirectory in the ‘Config’ folder: 
# '<project>/Config/_<environment>/variables.yaml
#
Variables:
  com:
    axonivy:
      utils:
        db:
          # name of database
          database: ""
          # URL to find scripts (supported are file: and classpath: URLs)
          scriptsurl: "classpath:/resources/sql/incremental"
          # URL to find blob data files (supported are classpath: URLs)
          dataurl: "classpath:/resources/data"
          # resource path of liquibase changelog file
          liquibasechangelog: "/resources/liquibase/changelog.yaml"
          # should the update scripts be automatically updated and executed at application start?
          autoupdate: true
          # should the tab to run incremental scripts be shown?
          incrementaltab: true
          # should the tab to execute SQL commands be shown?
          sqlstatementtab: true
          # should the tab to execute Excel export/import be shown?
          excelexportimporttab: true
```

- No information was delivered for this section.

## Components

### Connector Processes

- No information was delivered for this section.

### Form Components

#### DbUtilsData — Backing data for the DbUtils HTML dialog
- **Namespace:** com.axonivy.utils.db.DbUtils
- **Component type:** Data Class (used by HTML_DIALOG)
- **Fields:**
   - `ctrl` (com.axonivy.utils.db.controller.DbUtilsController) — Controller instance used by the UI to interact with DB utilities
- **Where used:** DbUtilsProcess.p.json

#### ScriptTableData — Backing data for the ScriptTable component
- **Namespace:** com.axonivy.utils.db.ScriptTable
- **Component type:** Data Class / JSF Composite
- **Fields:**
   - `ctrl` (com.axonivy.utils.db.controller.ScriptTableController) — Controller used to render the script table UI and run scripts
- **Where used:** ScriptTableProcess.p.json, ScriptTable.xhtml

### Maven artifacts

1. db-utils

```xml
<dependency>
  <groupId>com.axonivy.utils.db</groupId>
  <artifactId>db-utils</artifactId>
  <version>@version@</version>
  <type>iar</type>
</dependency>
```

2. db-utils-demo

```xml
<dependency>
  <groupId>com.axonivy.utils.db</groupId>
  <artifactId>db-utils-demo</artifactId>
  <version>@version@</version>
  <type>iar</type>
</dependency>
```
