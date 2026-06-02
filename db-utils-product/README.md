# DB-Utils

Db-Utils is a collection of tools to help with typical database tasks in your project. It comes with support for automatic, incremental SQL updates of database tables, export and import of data and a simple database query window. Support for Microsoft SQL and HSQLDB is provided out of the box, but it is easy to extend the component for other database types.

![DbUtils UI](images/gui.png)

## Key features

- Apply incremental SQL updates automatically to keep database schemas in sync.
- Run ad-hoc SQL statements and queries from the UI for troubleshooting and diagnostics.
- Import and export datasets via Excel for quick data migration and reporting.
- Perform Liquibase-based migrations with optional autostart behavior.
- Manage and run database scripts from a script table UI with run/refresh/enable actions.
- Supports HSQLDB and Microsoft SQL Server out of the box and is easy to extend.

## Demo

Check the included demo implementations to try Db-Utils with HSQLDB and MSSQL.

### Demo Workflows

#### db-utils-demo (db-utils-demo)

##### DbUtils HSQLDB

1. Launch the "DbUtils HSQLDB" demo from the demo menu.
2. The DbUtils dialog opens showing tabs for Incremental Updates, Liquibase, SQL Statement, Excel Import/Export and Settings.
3. Use the Incremental Updates tab to apply incremental SQL updates from the configured scripts location.
4. Use the SQL Statement tab to run ad‑hoc queries and review the output in the Messages area.
5. Optionally, use the Excel Import/Export tab to import or export datasets.

##### DbUtils MSSQL

1. Launch the "DbUtils MSSQL" demo from the demo menu.
2. The DbUtils dialog opens showing the same operation tabs configured for MSSQL.
3. Use the Liquibase autostart and Incremental Updates to initialize or update the database.
4. Run SQL statements or import/export data as needed.
5. Review messages and logs for operation results.

## Setup

- **Roles:** Everybody (configured in config/roles.xml)
- **OpenAPI:** No information was delivered for this section.

### Variables

```
@variables.yaml@
```


## Components

### Callable Subprocesses

- No connector processes delivered by this extension.

### Dialog Components

#### DbUtils — Provides a UI for common database operations: incremental updates, Liquibase migrations, SQL execution, Excel import/export, and settings.
- **Namespace:** com.axonivy.utils.db.DbUtils
- **Component type:** UI dialog
- **Fields:**
  - `dbUtilsResolver` (com.axonivy.utils.db.resolver.DbUtilsResolver) — 
- **Purpose:** Provides a UI for common database operations, including incremental updates, Liquibase migrations, SQL execution, Excel import/export, and settings.

#### ScriptTable — Manage and run database scripts (view, run, enable/disable)
- **Namespace:** com.axonivy.utils.db.ScriptTable
- **Component type:** Component dialog
- **Fields:**
  - `ctrl` (com.axonivy.utils.db.controller.ScriptTableController) — 
- **Purpose:** Displays configured scripts and lets you run, refresh, enable, or disable them.

### Web Services

- No information was delivered for this section.

### Maven Artifacts

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
