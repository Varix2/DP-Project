@echo off

echo Compiling Java files...
javac src\Project\client\Client.java ^
      src\Project\client\data\ClientAuthenticationData.java ^
      src\Project\client\data\ClientRegistryData.java ^
      src\Project\client\exceptions\AuthenticationErrorException.java ^
      src\Project\client\ui\AdminMenus.java ^
      src\Project\client\ui\TextUI.java ^
      src\Project\principalServer\concurrentServices\MulticastService.java ^
      src\Project\principalServer\concurrentServices\TCPService.java ^
      src\Project\principalServer\data\Heardbeat.java ^
      src\Project\principalServer\HeardbeatObserversInterface.java ^
      src\Project\principalServer\PrincipalServer.java ^
      src\Project\principalServer\PrincipalServerInterface.java ^
      src\Project\manageDB\data\Attendance.java ^
      src\Project\manageDB\data\Event.java ^
      src\Project\manageDB\DbOperations.java ^
      src\Project\manageDB\DbOperationsInterface.java

echo Running PrincipalServer...
java -cp src;lib\sqlite-jdbc-3.43.0.0.jar Project.principalServer.PrincipalServer 5000 C:\sqlite\db\ serverService 4444

echo Done.
pause