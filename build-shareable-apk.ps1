param(
    [string]$ProjectRoot = $PSScriptRoot
)

$ErrorActionPreference = "Stop"

$toolsDir = Join-Path $ProjectRoot "tools"
$sdkRoot = Join-Path $ProjectRoot "android-sdk"
$gradleRoot = Join-Path $toolsDir "gradle-8.7"
$gradleZip = Join-Path $toolsDir "gradle-8.7-bin.zip"
$cmdToolsZip = Join-Path $toolsDir "commandlinetools-win.zip"
$distDir = Join-Path $ProjectRoot "dist"

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
New-Item -ItemType Directory -Force -Path $sdkRoot | Out-Null

if (!(Test-Path $gradleRoot)) {
    Write-Host "Downloading Gradle 8.7..."
    Invoke-WebRequest "https://services.gradle.org/distributions/gradle-8.7-bin.zip" -OutFile $gradleZip
    Expand-Archive -Path $gradleZip -DestinationPath $toolsDir -Force
}

$cmdlineToolsLatest = Join-Path $sdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"
if (!(Test-Path $cmdlineToolsLatest)) {
    Write-Host "Downloading Android command-line tools..."
    Invoke-WebRequest "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" -OutFile $cmdToolsZip
    $tempExtract = Join-Path $toolsDir "cmdline-tools-extract"
    Remove-Item -Recurse -Force $tempExtract -ErrorAction SilentlyContinue
    Expand-Archive -Path $cmdToolsZip -DestinationPath $tempExtract -Force
    New-Item -ItemType Directory -Force -Path (Join-Path $sdkRoot "cmdline-tools") | Out-Null
    Remove-Item -Recurse -Force (Join-Path $sdkRoot "cmdline-tools\\latest") -ErrorAction SilentlyContinue
    Move-Item (Join-Path $tempExtract "cmdline-tools") (Join-Path $sdkRoot "cmdline-tools\\latest")
}

@"
sdk.dir=$($sdkRoot -replace '\\','\\')
"@ | Set-Content (Join-Path $ProjectRoot "local.properties")

$env:ANDROID_SDK_ROOT = $sdkRoot
$env:ANDROID_HOME = $sdkRoot

$sdkmanager = Join-Path $sdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"
Write-Host "Installing Android SDK packages..."
cmd /c "echo y | `"$sdkmanager`" --licenses" | Out-Null
& $sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

$gradleBat = Join-Path $gradleRoot "bin\gradle.bat"
Write-Host "Building release APK..."
& $gradleBat -p $ProjectRoot clean :app:assembleRelease

$apkSource = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
$apkTarget = Join-Path $distDir "WhatsAppChatAnalyzer-release.apk"
Copy-Item -Force $apkSource $apkTarget

Write-Host ""
Write-Host "Done. Shareable APK:"
Write-Host $apkTarget
