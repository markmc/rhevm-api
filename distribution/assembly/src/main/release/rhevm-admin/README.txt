
Using the RHEVM-API Command Line Interface
==========================================

The RHEVM-API CLI is a demonstration client based on the Apache Felix Gogo
shell running in the Apache Felix Karaf OSGi runtime.


Running the CLI
---------------

The CLI is launched as follows (in a normal DOS command shell,
as opposed to a PowerShell instance):

 > cd rhevm-admin\bin
 > rhevm-admin

An interactive shell will appear with command recall, tab-completion
and integrated help.

Commands are scoped according the RHEV entity type (vms, hosts,
datacenters etc). This scope is followed by a verb (list, update,
stop, start, etc) possibily with options and/or arguments.

The very first time you run it, allow Karaf a few seconds to activate
all the system bundles. Then the RHEVM commands will become available
via tab-completion, so for example v<TAB>l<TAB> will resolve to "vms:list",
whereas v<TAB>sh<TAB>vm<TAB> will resolve to vms:shutdown with a list
of VM names to choose from.

The following command scopes are relevant to RHEV-M:

  vms, vmpools, templates, actions, cpus, hosts,
  clusters, datacenters, storagedomains, networks

The --help option is supported with each command giving basic
man-style documentation.

By default the base URL used includes the port 8080, but this can be
over-ridden, for example by setting:

 > set RHEVM_API_URL=https://localhost:8443/rhevm-api-powershell

To exit the CLI, use the shutdown command.


Authentication
--------------

RHEVM_API uses HTTP Basic authenication to pass RHEVM credentials from
the client. In order to provide the  correct user name and password, the
following variables are set:

  > set RHEVM_USERNAME=username
  > set RHEVM_PASSWORD=password

The Active Directory domain may be embedded in the username via the usual
convention:

  username@domainname

See ..\README_HTTPS.txt for further details on HTTPS setup.


Scripting with the CLI
----------------------

The CLI console is interactive and not well suited to scripting,
so we also provide a lightweight client to submit commands to the
rhevm-admin process via ssh.

So if rhevm-admin is already running, the client can be lauched for
example as follows:

 > rhevm-client vms:list

This client form is more suitable for shell constructs such as:

  for /f "usebackq tokens=1 delims=[] " %v in (`rhevm-client.bat "vms:list"`) do rhevm-client.bat "vms:start %v"
