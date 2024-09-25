<!--
Dear developer!     

When you create your very valuable documentation, please be aware that this Readme.md is not only published on github. This documentation is also processed automatically and published on our website. For this to work, the two headings "Demo" and "Setup" must not be changed
-->

# DB-Utils

DB-Utils is a collection of tools to help with typical database-tasks in your project.
It comes with support to implement incremental auto-update of tables, export and import
of data and a simple query GUI.
Support for MS SQL and HSQLDB is provided out of the box, but it is easy to extend the
component for other database types.

<!--
The explanations under "MY-RRODUCT-NAME" are displayed  e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-description   
-->

## Demo

The demo shows how to integrate Db-Utils in your project.

<!--
We use all entries under the heading "Demo" for the demo-Tab on our Website, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-demo  
-->

## Setup

To use the tools, you must provide a class implementing the interface `com.axonivy.utils.db.resolver.DbUtilsResolver`. Ready to use (inherit from) resolvers
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

The following global variables exist:
<!--
The entries under the heading "Setup" are filled in this tab, e.g. for the Connector A-Trust here: https://market.axonivy.com/a-trust#tab-setup. 
-->

```
@variables.yaml@
```