<!--
Dear developer!     

When you create your very valuable documentation, please be aware that this Readme.md is not only published on github. This documentation is also processed automatically and published on our website. For this to work, the two headings "Demo" and "Setup" must not be changed
-->

# DB-Utils

<!--
The explanations under "MY-RRODUCT-NAME" are displayed  e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-description   
-->

DB-Utils is a collection of tools to help with typical database-tasks in your project.
It comes with support to implement incremental auto-update of tables, export and import
of data and a simple query GUI.
Support for MS SQL and HSQLDB is provided out of the box, but it is easy to extend the
component for other database types.

## Features

The most important feature of DB-Utils is probably the automatic update of your database whenever you deploy. Additionally, data can be easily exported or imported into Excel or Zip files and simple queires can be executed directly within your application. By defining a resolver to provide your project setup, some settings in global variables and potentially a process start event bean, you can make use of all features of DB-Utils.

### Incremental Updates

DB-Utils works by maintaining a list of incremental SQL script files and their execution status together with your project and your project#s database. These files can be stored in a file-system folder or in a resources directory (which is the preferred way). As a convention, SQL scripts are sorted, displayed and executed in alphabetical order of their file-names. It is recommended to put the incremental files in a subfolder of the `src` folder of your project (e.g. `src/resources/sql/incremental`) and follow a common pattern when naming your scripts, e.g.

`YYYYMMDD-HHMM-Ticket-Short-Description.sql`

DB-Utils creates it's own table to remember, which of these files were executed and provides a GUI to display the list of scripts together with their status. Scripts can be executed, skipped and generally maintained in this GUI.

Additionally you can define a `ch.ivyteam.ivy.process.eventstart.IProcessStartEventBean` to execute all scripts needed
automatically during the start of your application. This `ch.ivyteam.ivy.process.eventstart.IProcessStartEventBean` can be created easily by simply inheriting from `com.axonivy.utils.db.AbstractDbUtilsStartEventBean`. Note, that this bean has to be defined in the context of your application, since it has to "know" about (have a dependency to) the resources of your project.

### SQL Queries

DB-Utils offers a simple GUI to execute SQL scripts. Note, that these scripts are executed "as-is" without any checks with the
permission of the configured user. The GUI is rudimentary and the idea is to use it for quick lookups or fixes.

### Excel Export and Import

DB-Utils offers an export and import functionality for Execl files and even binary BLOBS. This feature is implemented by DB-Unit.

Exporting can be done in two ways:

* *Export Excel*: Export an Excel with one sheet per table
* *Export ZIP*: Export an Excel with one sheet per table, but additionally export all columns representing a binary large object (BLOB) as an own file. The Excel and all exported files are stored in a ZIP file. in the ZIP file, BLOB column files are put into subfolders with the naming convention `lob/<TABLE>/<COLUMN>`.

Importing can be done with or without cleaning the database first. Note, that this is a potentially dangerous operation as deletion of entries cannot be undone. Importing data should probably onyl be used during tests to put a database into a defined test state or for an initial setup of your project on a new machine.

* *Load Excel*: Load an Excel in the same format as the Export creates.
* *Load Excel and handle classpath blobs*: Currently, a previously exported ZIP file cannot be imported but a solution is provided which proved useful in our tests. The Import loads an Excel in the same format as Export ZIP creates but handle classpath references in Excel columns. Whenever a column contains a classpath reference, the file is looked up in the *data resources* defined for DB-Utils and the file will be inserted as a Blob. It is recommended to put the BLOB files in a subfolder of the `src` folder of your project (e.g. `src/data`). The assumption is, that you will only have a few seldomly changing BLOBs in your project for testing and don't want to create ZIP files for every column change in the imported Excel during development.
 
### Settings

The settings page shows some basic database settings to find out, which database is in use. It might later be extended.

### Support for multiple databases

Multiple databases are supported. Every database would need it's own resolver and it's own process start event bean. Of course, the default mechanism to get configuration from global variables can only be used for one database. If you provide multiple resolvers, also implement handling of different configurations (by using constants, global variables,...).

## Demo

The demo shows how to integrate Db-Utils in your project.

<!--
We use all entries under the heading "Demo" for the demo-Tab on our Website, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-demo  
-->

## Setup

To use the tools, you must provide a class implementing the interface `com.axonivy.utils.db.resolver.DbUtilsResolver`. It is important, that this class is implemented in a project of your application, sonce it has to "know" about (have a dependency to) your database and resources. Ready to use (inherit from) resolvers
are provided for
* Microsoft SQL: `com.axonivy.utils.db.resolver.MSSQL2005DbUtilsResolver`
* HSQLDB: `com.axonivy.utils.db.resolver.HSQLDbUtilsResolver`

For other databases, you can start by inheriting from `com.axonivy.utils.db.resolver.AbstractDbUtilsResolver` and adapt similar to
the Microsoft or HSQLDB implementations. Your implementation must mainly care about
creation of the table used to store script information.

Classes inherting from `com.axonivy.utils.db.resolver.AbstractDbUtilsResolver` can be
configured by global variables. This configuration can be overridden in your implementation
(e.g. you can get cour configuration differently, if you have more than one database to
maintain in your project).

If you go with the default implementation, the most important global variables (settings) are:
* Name of database as defined in the Ivy database configuration.
* URL to find incremental SQL scripts. These scripts can be in the file-system, but a more convenient way is to put them into your project as a resource, by using the `classpath` scheme. In this way, they will automatically be deployed and always upd-to-date with your project.
* Data URL. This is the resource path used to search for binary files which can be reference in Excel imports.

### Security

Since you decide where to use the GUI, it is also in your responsibility to secure the starting process with
a special DB administrative role. Additionally, the tabs of the DB GUI can be switched on and off by configuration.

Note, that features like quering the database or performing DDL operations are done with the user defined in your database configuration, so they are restricted by the permissions this user has been given. In some scenarios you could consider defining extra database connections with more permissions only for use by DB-Utils and not the application.

<!--
The entries under the heading "Setup" are filled in this tab, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-setup. 
-->

```
@variables.yaml@
```
