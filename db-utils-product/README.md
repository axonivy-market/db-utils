<!--
Dear developer!     

When you create your very valuable documentation, please be aware that this Readme.md is not only published on github. This documentation is also processed automatically and published on our website. For this to work, the two headings "Demo" and "Setup" must not be changed
-->

# DB-Utils

<!--
The explanations under "MY-RRODUCT-NAME" are displayed  e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-description   
-->

Db-Utils is a collection of tools to help with typical database-tasks in your project.
It comes with support for automatic, incremental SQL updates of database tables, export
and import of data and a simple database query window. Support for MS SQL and HSQLDB is
provided out of the box, but it is easy to extend the component for other database types.

## Concepts

The most important feature of DB-Utils is probably the automatic update of your database whenever you deploy. Additionally, data from your databases can be easily exported or imported into Excel or Zip files and simple queries can be executed directly from a Db-Utils GUI within your application. By defining a resolver to provide your project setup, some settings in global variables and potentially a process start event bean, you can make use of all features of DB-Utils.

### Incremental Updates

Db-Utils works by maintaining a list of incremental SQL scripts and their execution status together with your project and your project’s database. Files can be executed manually from the Db-Utils GUI or automatically whenever your application starts. The SQL scripts can be stored in a file-system folder or in a resources (classpath) directory (which is the preferred way). As a convention, SQL scripts are sorted, displayed and executed in the alphabetical order of their filenames. It is recommended to put the project’s incremental files into the classpath of your project e.g. a subfolder of the `src` folder of your project (e.g. `src/resources/sql/incremental`) and follow a common pattern when naming your scripts, e.g.

`YYYYMMDD-HHMM-Ticket-Short-Description.sql`

Db-Utils creates a table to remember, which of these SQL scripts were executed and provides a GUI to display the list of scripts together with their status. Scripts can be executed, skipped and generally maintained in this GUI.

Additionally, you can define a IProcessStartEventBean to execute needed (not yet executed) SQL scripts automatically in the correct order during the start of your application. This IProcessStartEventBean can be created easily by simply extending AbstractDbUtilsStartEventBean. Note, that this bean must be defined in the context of your application (or depend on your projectes), since it must have access to the classpath of your projects.

### SQL Queries

Db-Utils offers a simple GUI to execute SQL scripts. Note, that these scripts are executed "as-is" without any checks and under the permissions of the user configured for your database. The GUI displays results in a simple text window. It is designed for quick small lookups or online fixes and does not compare to any real database tool.

### Excel Export and Import

Db-Utils offers an export and import functionality for Excel files and even binary BLOBS. This feature is implemented by [DbUnit](https://www.dbunit.org/).

**Export of data** can be done in two ways:
* *Export Excel* Export an Excel with one sheet per table
* *Export ZIP* Export an Excel with one sheet per table, but additionally export all columns representing a binary large object (BLOB) into their own file. The Excel and all exported files are stored in a ZIP file. in the ZIP file, BLOB column files are put into subfolders with the naming convention `lob/<TABLE>/<COLUMN>/file.ext`.

**Import of data** can be done with or without cleaning the database first. Note, that this is a potentially dangerous operation as deletion of entries cannot be undone. Importing data should probably only be used during tests to put a database into a defined test state or for an initial setup of your project database on a new machine.
* *Load Excel* Load an Excel in the same format as the Export creates.
* *Load Excel* and handle classpath blobs: Currently, a previously exported ZIP file cannot be imported but a solution is provided which proved useful in project developments. The Import loads an Excel in the same format as Export ZIP creates but handle classpath references in Excel columns. If a column contains a classpath reference (`classpath:/path`), the file is looked up in the data resources defined for DB-Utils and the file will be inserted as a Blob. It is recommended to put the BLOB files in a subfolder of the src folder of your project (e.g. `src/data`). The assumption is, that you will only have a few seldomly changing BLOB test files in your project for testing and don't want to create ZIP files for every column change in the imported Excel during development.

Note, that for importing, the sheets in your Excel must be in the right order to not break any constraints. To get the right order, it is best to export the database first. Export will create an Excel with the right sheet order.
 
### Settings

The settings page shows some basic database settings to find out, which database is in use. It might later be extended.

### Support for multiple databases

Multiple databases are supported. Every database would need its own resolver, and its own process start event bean. Of course, the default mechanism to get configuration from global variables can only be used for one database. If you provide multiple resolvers, also implement handling of different configurations (by using constants, global variables…).

## Demo
<!--
We use all entries under the heading "Demo" for the demo-Tab on our Website, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-demo  
-->

The demo shows how to integrate Db-Utils in your project. It contains the simplest possible configuration for a HSQLDB and a little bit more elaborated configuration for a MS SQL Server database. For the MS SQL Server part you need to have access to an MS SQL Server database (unpack the project to configure for your database). Note, that Db-Utils will unconditionally execute SQL statements against this database!

### Db-Utils GUI

Most of the demo can be seen in the Db-Utils GUI. The GUI has tabs for different operations and a common message area to show results.

### Incremental Updates

The **Incremental Updates** tab show an overview of available SQL scripts and an overview of scripts which once have been there but are no longer available as resources. For every script the execution date, errors and a status are shown. Scripts can be manually executed, refreshed, disabled, ignored or deleted (only available for unavailable scripts).

Shortcuts are available to run all scripts which were not yet successfully executed and even to force continuing in case of errors.

![Incremental updates](images/incremental.png)

### SQL Statements

The **SQL Statements** tab can be used to execute simple SQL statements against the database. Results are shown in the message area.

![SQL Statements](images/sql.png)

### Excel Export/Import

The **Excel Export/Import** tab is used to export the whole database to an Excel or ZIP file or to import the whole database (or parts) from an Excel file. When importing, you can select to clean the database before importing. Note: this cleanup will clean all tables mentioned in the imported Excel unconditionally. It is possible to import incremental, if you do not break any database constraints.

![Excel Export and Import](images/eximport.png)

### Settings

The **Settings** tab shows the current settings used by Db-Utils.

![Settings](images/settings.png)

## Setup
<!--
The entries under the heading "Setup" are filled in this tab, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-setup. 
-->

To integrate and use DB-Utils in your project, you must (for every database you want to support)
* provide a project local `DBUtilsResolver` class
* provide a project local `DbUtilsStartEventBean` class
* create a start process which calls the DB-Utils GUI
* create a program start using the `DbUtilsStartEventBean`
* check configuration
* check security

In the demo project, you will find examples for a simple setup (HSQLDB part) and a slightly more complex, adapted setup (Microsoft SQL Server part). Please compare the following description to these examples.

### Provide `DbUtilsResolver`

The DbUtilsResolver is used to keep all configuration information for one of the databases defined in your project (e.g. name, resource paths, DBUtilsScript table definition…). It is essential, that this class is implemented in a project which either defines or has a dependency to a project defining your database and script resources. It can be implemented by extending the `AbstractDbUtilsResolver` class provided by DB-Utils. Implementations for Microsoft SQL Server (`MSSQL2005DbUtilsResolver`) and HSQLDB (`HSQLDbUtilsResolver`) are provided directly by DB-Utils.

### Provide DbUtilsStartEventBean

The `DbUtilsStartEventBean` is used as the Java class in an event process start. It should extend `AbstractDbUtilsStartEventBean` which is provided by DB-Utils and implement a default constructor which must set the projects `DbUtilsResolver`.

### Create Db-Utils GUI process start

Create a start process which uses the Db-Utils GUI (and the projects `DbUtilsResolver`) provided by the Db-Utils project (see below). Note, that you should secure this start by an authorized role of the project!

![GUI Integration](images/gui.png)

### Create Program Start

Create a program start which uses the projects `DbUtilsStartEventBean` (see below).

![Process Start Event Bean](images/starteventbean.png)

### Configuration

Classes extending `AbstractDbUtilsResolver` can be configured by global variables. The most important global variables (settings) are:
* Name of database as defined in the Ivy database configuration.
* The Script URL to find incremental SQL scripts. These scripts can be in the file-system, but a more convenient way is to put them into your project as a resource, by using the classpath scheme in the URL. In this way, they will automatically be deployed and always up-to-date with your project.
* The Data URL used for other data, e.g. for binary files which can be used in Excel BLOB imports.
* Additional Settings to configure automatic updates and enable or disable GUI tabs

```
@variables.yaml@
```

### Security

Db-Utils can be used to execute arbitrary SQL scripts without further checks directly to the configured database with permissions of the configured user. It is therefore important to secure the Db-Utils GUI start with an elevated role in your project (`DbUtilsAdmin` or similar). Additionally it is possible to switch off any tab (functionality) in Db-Utils GUI by configuration.

To use the automatic update feature of Db-Utils, the configured database user will most likely need extended database permissions (e.g. to change table definitions) that you might not want to have in your application.

In this case it could be an idea, to create a separate project (e.g. a tools project) depending on your projects and put all Db-Utils specific implementation and a special, elevated database configuration in this separate project.


