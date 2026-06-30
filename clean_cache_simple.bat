@echo off

echo Cleaning Gradle cache...

rem Close Java processes
taskkill /F /IM java.exe 2>nul
taskkill /F /IM studio64.exe 2>nul
taskkill /F /IM gradle.exe 2>nul

echo Deleting Gradle jars cache...
rd /s /q "%USERPROFILE%\.gradle\caches\jars-9" 2>nul

echo Deleting project build files...
rd /s /q "build" 2>nul
rd /s /q "app\build" 2>nul

echo Cleanup completed!
echo Please restart Android Studio and sync the project.
