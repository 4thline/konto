==============================================================================
Konto - a double-entry ledger accounting system
==============================================================================

Version: ${project.version}

Get a Servlet 2.5 compatible container and deploy the WAR. An in-memory
temporary H2 DBMS instance will be used.

If you want to us a permanent external H2 DBMS instance, unpack the
WAR and edit the WEB-INF/classes/hibernate.cfg.xml file. Then repack the
WAR and deploy. Also edit and deploy the /sql/database-init-schema.sql
as needed, import it on your DBMS.

If you want to use a different database, recreate the SQL file(s)
with "mvn package" after you edit the src/main/resources/hibernate.cfg.xml
file.

Feedback, bug reports: http://4thline.org/projects/mailinglists.html

Copyright (C) 2011 4th Line GmbH, Switzerland

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


This program may contain third-party code under various licenses. See the
source code copyright notices in the individual files for further details.
