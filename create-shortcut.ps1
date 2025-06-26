$WScriptShell = New-Object -ComObject WScript.Shell

# Путь к .exe
$exePath = Join-Path -Path $PSScriptRoot -ChildPath "build\launch4j\StegoApp.exe"
$iconPath = Join-Path -Path $PSScriptRoot -ChildPath "src\main\resources\stego-icon.ico"

Write-Host "EXE: $exePath"
Write-Host "ICON: $iconPath"

if (-Not (Test-Path $exePath)) {
    Write-Error "EXE не найден: $exePath"
    exit
}
if (-Not (Test-Path $iconPath)) {
    Write-Error "ICON не найден: $iconPath"
    exit
}

# Ярлык создаем в текущей папке
$shortcutPath = Join-Path $PSScriptRoot "StegoApp.lnk"
Write-Host "Создаем ярлык по пути: $shortcutPath"

try {
    $Shortcut = $WScriptShell.CreateShortcut($shortcutPath)
    $Shortcut.TargetPath = $exePath
    $Shortcut.IconLocation = $iconPath
    $Shortcut.WorkingDirectory = Split-Path $exePath
    $Shortcut.Description = "Stegonography"
    $Shortcut.Save()
    Write-Host " Ярлык успешно создан: $shortcutPath"
} catch {
    Write-Error " Не удалось создать ярлык: $_"
}
