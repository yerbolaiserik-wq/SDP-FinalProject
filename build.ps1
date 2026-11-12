$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $jdk = Join-Path $env:USERPROFILE '.jdks\openjdk-27'
    if (Test-Path "$jdk\bin\java.exe") { $env:JAVA_HOME = $jdk }
    $installed = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($installed) { $maven = $installed.Source }
    else {
        $bundled = Get-ChildItem 'C:\Program Files\JetBrains\*\plugins\maven\lib\maven3\bin\mvn.cmd' -ErrorAction SilentlyContinue | Select-Object -First 1
        if (-not $bundled) { throw 'Maven is required to build the test suite and JAR.' }
        $maven = $bundled.FullName
    }
    & $maven package
    if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
} finally { Pop-Location }
