

1) add a user for remote JMS access from 

W:\training6_scalabook\jboss-as-7.1.0.Final\bin>set JAVA_HOME=t:\jdk1.6.0_19

W:\training6_scalabook\jboss-as-7.1.0.Final\bin>add-user.bat

What type of user do you wish to add?
 a) Management User (mgmt-users.properties)
 b) Application User (application-users.properties)
(a): b

Enter the details of the new user to add.
Realm (ApplicationRealm) :
Username : ticketValidation
Password : password
Re-enter Password : password
What roles do you want this user to belong to? (Please enter a comma separated l
ist, or leave blank for none) : ticketValidationRole
About to add user 'ticketValidation' for realm 'ApplicationRealm'
Is this correct yes/no? yes
Added user 'ticketValidation' to file 'W:\training6_scalabook\jboss-as-7.1.0.Fin
al\standalone\configuration\application-users.properties'
Added user 'ticketValidation' to file 'W:\training6_scalabook\jboss-as-7.1.0.Fin
al\domain\configuration\application-users.properties'
Added user 'ticketValidation' with roles ticketValidationRole to file 'W:\traini
ng6_scalabook\jboss-as-7.1.0.Final\standalone\configuration\application-roles.pr
operties'
Added user 'ticketValidation' with roles ticketValidationRole to file 'W:\traini
ng6_scalabook\jboss-as-7.1.0.Final\domain\configuration\application-roles.proper
ties'
Press any key to continue . . .

W:\training6_scalabook\jboss-as-7.1.0.Final\bin>