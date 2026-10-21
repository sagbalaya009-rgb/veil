[CmdletBinding()]
param([string]$SdkPath = $env:ANDROID_HOME)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if ($SdkPath) { $env:ANDROID_HOME = $SdkPath }
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'JDK 17 must be installed and java must be on PATH.'
}
Push-Location -LiteralPath $repoRoot
try {
    & .\gradlew.bat --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease
    if ($LASTEXITCODE -ne 0) { throw "Gradle verification failed with exit code $LASTEXITCODE." }
    Get-ChildItem -LiteralPath app/build/outputs/apk/debug,app/build/outputs/apk/release -Filter *.apk |
        Get-FileHash -Algorithm SHA256 |
        Format-Table -AutoSize
} finally {
    Pop-Location
}
