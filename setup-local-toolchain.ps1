$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $projectRoot ".tools"
$javaApiUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
$mavenVersion = "3.9.15"
$mavenZipUrl = "https://downloads.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

function Download-File {
    param(
        [string]$Url,
        [string]$Destination
    )

    Write-Host "Downloading $Url"
    & curl.exe -L $Url -o $Destination
    if ($LASTEXITCODE -ne 0) {
        throw "Download failed for $Url"
    }
}

$javaZipName = "temurin-jdk-17.zip"
$javaZipPath = Join-Path $toolsDir $javaZipName

if (-not (Test-Path $javaZipPath)) {
    Download-File -Url $javaApiUrl -Destination $javaZipPath
}

$javaExtractName = [System.IO.Path]::GetFileNameWithoutExtension($javaZipName)
$javaTargetDir = Join-Path $toolsDir $javaExtractName
if (-not (Test-Path $javaTargetDir) -and -not (Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-*" -ErrorAction SilentlyContinue)) {
    Write-Host "Extracting $javaZipName"
    Expand-Archive -Path $javaZipPath -DestinationPath $toolsDir -Force
}

$mavenZipName = "apache-maven-$mavenVersion-bin.zip"
$mavenZipPath = Join-Path $toolsDir $mavenZipName
if (-not (Test-Path $mavenZipPath)) {
    Download-File -Url $mavenZipUrl -Destination $mavenZipPath
}

$mavenTargetDir = Join-Path $toolsDir "apache-maven-$mavenVersion"
if (-not (Test-Path $mavenTargetDir)) {
    Write-Host "Extracting $mavenZipName"
    Expand-Archive -Path $mavenZipPath -DestinationPath $toolsDir -Force
}

$javaHome = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-*" |
    Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $javaHome) {
    throw "JDK extraction completed, but no Java installation was found in $toolsDir."
}

$mvnCmd = Join-Path $mavenTargetDir "bin\mvn.cmd"
if (-not (Test-Path $mvnCmd)) {
    throw "Maven extraction completed, but mvn.cmd was not found at $mvnCmd."
}

Write-Host ""
Write-Host "Local toolchain is ready."
Write-Host "JAVA_HOME=$javaHome"
Write-Host "Maven=$mvnCmd"
Write-Host ""
Write-Host "Use one of these commands from the project folder:"
Write-Host '  .\mvnw.cmd javafx:run'
Write-Host '  .\run_mvn.bat javafx:run'
