$ws = New-Object -ComObject WScript.Shell
$sc = $ws.CreateShortcut([Environment]::GetFolderPath('Desktop') + '\知识竞答游戏.lnk')
$sc.TargetPath = 'D:\meaching_learning\个人学习\Java算法学习\game\start.bat'
$sc.WorkingDirectory = 'D:\meaching_learning\个人学习\Java算法学习\game'
$sc.Save()
Write-Host 'Done'
