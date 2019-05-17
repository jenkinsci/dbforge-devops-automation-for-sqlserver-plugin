#dbForge DevOps Automation for SQL Server Plugin

##Introduction

Devart dbForge DevOps Automation for SQL Server Plugin is an open-source plugin for using [dbForge DevOps Automation PowerShell for SQL Server](https://www.devart.com/dbforge/sql/) in Jenkins. There are four tasks available:

1. Build - deploys a database on LocalDB or on a specified SQL Server and generates a NuGet package from a Source control repository.
2. Test - runs tSQLt and generates test data.
3. Sync - deploys a NuGet package and synchronizes it with a working DB.
4. Publish - places a NuGet Package to a NuGet feed for further deployment.

##Installing
If you want to use just the plugin, perform the following steps:

1. Open your Jenkins.
2. Go to Manage Jenkins -> Manage Plugins -> Available, and search for Devart.
3. Select dbForge DevOps Automation for SQL Server Plugin, and click 'Install Without Restart'

##How to build/debug the plugin.

A basic tutorial for developing plugins is available at https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial.

Perform the following steps:

1. Clone this repository.
2. Install Maven.
3. Open a command prompt in the repository root directory. Then run:
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
mvn hpi:run 
4. Install the dbForge DevOps Automation PowerShell module from [powershell gallery](https://www.powershellgallery.com/packages/Devart.DbForge.DevOpsAutomation.SqlServer) or [dbForge DevOps Automation PowerShell for SQL Server](https://www.devart.com/dbforge/sql/).

JetBrains IntelliJ IDEA is a good environment for developing and debugging Jenkins plugins. There is a free community edition.

##Help Us to Make it Better
If you have any ideas of how to improve dbForge DevOps Automation for SQL Server Plugin, submit a pull request with your code to us. Upon approval, we WILL IMPLEMENT the suggested changes.

You can also email us at support@devart.com