param([switch]$Demo)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $jdk = Join-Path $env:USERPROFILE '.jdks\openjdk-27'
    $java = if (Test-Path "$jdk\bin\java.exe") { "$jdk\bin\java.exe" } else { 'java' }
    $javac = if (Test-Path "$jdk\bin\javac.exe") { "$jdk\bin\javac.exe" } else { 'javac' }
    $sources = Get-ChildItem -Recurse -Filter '*.java' src\main\java
    New-Item -ItemType Directory -Force build\classes | Out-Null
    & $javac --release 17 -d build\classes $sources.FullName
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
    $arguments = @('-cp', 'build\classes', 'com.example.restaurant.Main')
    if ($Demo) { $arguments += '--demo' }
    & $java @arguments
    if ($LASTEXITCODE -ne 0) { throw 'Application failed.' }
} finally { Pop-Location }
