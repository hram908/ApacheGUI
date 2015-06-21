@echo off
IF DEFINED APACHEGUIJRE_HOME GOTO DEFINED
IF EXIST jre1.8.0_31 GOTO SETAPACHEGUIJRE
IF DEFINED JAVA_HOME GOTO DEFINED
IF DEFINED JRE_HOME GOTO DEFINED
ECHO JAVA_HOME and JRE_HOME are NOT defined, one of these environment variables must be defined.
(PAUSE)
GOTO END
:SETAPACHEGUIJRE
set APACHEGUIJRE_HOME=%~dp0jre1.8.0_31
setx APACHEGUIJRE_HOME "%~dp0jre1.8.0_31"
:DEFINED
set TOMCAT_HOME=..\tomcat
cd %TOMCAT_HOME%\bin
call elevate.exe run.bat
cd ../../bin
:END
