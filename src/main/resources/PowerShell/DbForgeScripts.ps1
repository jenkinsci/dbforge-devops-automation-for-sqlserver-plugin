# это пример вызова из cmd
# powershell -NonInteractive -ExecutionPolicy Bypass -command "& { . D:\Projects\JenkinsPlugs\Devops\SqlServer\DbForgeJenkinsPlugin\src\main\resources\PowerShell\ModuleScript.ps1; CheckRequiredComponentInstall SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\DevartSchemaCompareMSSql_is1 DisplayVersion 4.6.27}" -Verbose

Function CheckRequiredComponentInstall($key, $name, $value)
{
  $ErrorActionPreference = "Stop"
  $regValue = $false
  $type = [Microsoft.Win32.RegistryHive]::LocalMachine
  try
  {
    $regKey = [Microsoft.Win32.RegistryKey]::OpenBaseKey($type, [Microsoft.Win32.RegistryView]::Registry64)
    $regSubKey =  $regKey.OpenSubKey($key, $false)

    if ($regSubKey -eq $null){
      $regKey = [Microsoft.Win32.RegistryKey]::OpenBaseKey($type, [Microsoft.Win32.RegistryView]::Registry32)
      $regSubKey =  $regKey.OpenSubKey($key, $false)
    }
    if ($regSubKey -eq $null){
      [System.Environment]::Exit(1)
    }
    $regValue = $regSubKey.GetValue($name)

    if ([version]$value -le [version]$regValue){
      [System.Environment]::Exit(0)
    }
    [System.Environment]::Exit(1)
  }
  catch
  {
    Write-Host $_.Exception.Message -ForegroundColor Red
    [System.Environment]::Exit(1)
  }
}

Function CheckPowerShellInstall($name, $version)
{
  try
  {
    $dbForgeDevopsPSModule = Get-Module -ListAvailable -Name $name
    if ($dbForgeDevopsPSModule.Version -ge $version) {
        [System.Environment]::Exit(0)
    }
    [System.Environment]::Exit(1)
  }
  catch
  {
    Write-Host $_.Exception.Message -ForegroundColor Red
    [System.Environment]::Exit(1)
  }
}

Function CreateLocalDbInstance($localDbName)
{
  $ErrorActionPreference = "Stop"

  try
  {
    $arguments = "create {0}"–f $localDbName

    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = "SqlLocalDB.exe"
    $pinfo.RedirectStandardError = $true
    $pinfo.RedirectStandardOutput = $true
    $pinfo.UseShellExecute = $false
    $pinfo.Arguments = $arguments
    $pinfo.WindowStyle = "Hidden"
    $p = New-Object System.Diagnostics.Process
    $p.StartInfo = $pinfo
    $p.Start() | Out-Null
    $p.WaitForExit()

    Write-Host $p.StandardOutput.ReadToEnd()
    if($p.ExitCode -ne 0) {
      Write-Host $p.StandardError.ReadToEnd()
    }
    [System.Environment]::Exit($localdbProcess.ExitCode)
  }
  catch
  {
    Write-Host $_.Exception.Message -ForegroundColor Red
    [System.Environment]::Exit(1)
  }
}

Function DeleteLocalDbInstance($localDbName)
{
  $ErrorActionPreference = "Stop"

  try
  {
    $arguments = "stop {0} -k"–f $localDbName
    $localdbProcess = Start-Process -FilePath "SqlLocalDB.exe" -ArgumentList $arguments -PassThru -Wait -WindowStyle Hidden
    if($localdbProcess.ExitCode -ne 0) {
      [System.Environment]::Exit($localdbProcess.ExitCode)
    }

    $arguments = "delete {0}"–f $localDbName
    $localdbProcess = Start-Process -FilePath "SqlLocalDB.exe" -ArgumentList $arguments -PassThru -Wait -WindowStyle Hidden
    [System.Environment]::Exit($localdbProcess.ExitCode)
  }
  catch
  {
    Write-Host $_.Exception.Message -ForegroundColor Red
    [System.Environment]::Exit(1)
  }
}