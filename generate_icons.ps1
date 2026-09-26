Add-Type -AssemblyName System.Drawing

$sourceImagePath = "C:\Users\xyzai\.gemini\antigravity\brain\1c972d06-16f7-4a15-a801-8f3b64ce81b3\drift_app_icon_1790451631704.jpg"
$baseDir = "app\src\main\res"

$sizes = @{
    "mdpi" = 48
    "hdpi" = 72
    "xhdpi" = 96
    "xxhdpi" = 144
    "xxxhdpi" = 192
}

$sourceImage = [System.Drawing.Image]::FromFile($sourceImagePath)

foreach ($size in $sizes.GetEnumerator()) {
    $folderName = "mipmap-$($size.Key)"
    $folderPath = Join-Path $baseDir $folderName
    
    if (-not (Test-Path $folderPath)) {
        New-Item -ItemType Directory -Path $folderPath | Out-Null
    }

    $dim = $size.Value
    $bmp = New-Object System.Drawing.Bitmap $dim, $dim
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.DrawImage($sourceImage, 0, 0, $dim, $dim)
    
    $outputPath1 = Join-Path $folderPath "ic_launcher.png"
    $outputPath2 = Join-Path $folderPath "ic_launcher_round.png"
    
    $bmp.Save($outputPath1, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save($outputPath2, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $graphics.Dispose()
    $bmp.Dispose()
}

$sourceImage.Dispose()

Remove-Item -Path "$baseDir\mipmap-anydpi-v26" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$baseDir\mipmap-anydpi-v33" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$baseDir\drawable\ic_launcher_background.xml" -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$baseDir\drawable-anydpi-v24\ic_launcher_foreground.xml" -Force -ErrorAction SilentlyContinue
