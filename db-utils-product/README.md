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

DB-Utils can be used to maintain a list of incremental SQL script files and their execution status together with your project. These files can be stored in a files or resources directory. As a convention, SQL scripts are executed in alphabetical order of their file-names. It is recommended to follow a common pattern when naming your scripts, e.g. `YYYYMMDD-HHMM-Ticket-Short-Description.sql`. DB-Utils creates it's own table to remember, which of these files were executed and provides a GUI to display the list of scripts together with their status. Scripts can be executed and maintained in this GUI.

Additionally you can define a `ch.ivyteam.ivy.process.eventstart.IProcessStartEventBean` to execute all scripts needed
directly at the start of your application. This `ch.ivyteam.ivy.process.eventstart.IProcessStartEventBean` can be created easily by simply inheriting from `com.axonivy.utils.db.AbstractDbUtilsStartEventBean`. It is important, that this bean is defined in the context of your application, since it has to "know" about (have a dependency to) the resources of your project.


### SQL Queries

### Excel Export and Import

### Support for multiple databases


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
