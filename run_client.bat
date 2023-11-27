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

echo Running Client...
java -cp src Project.client.Client 127.0.0.1 5000

echo Done.
pause