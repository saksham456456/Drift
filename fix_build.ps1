$repo = "app/src/main/java/com/example/anonymouschat/data/DataRepository.kt"
(Get-Content $repo) -replace '@Singleton', '' -replace '@Inject constructor\(\)', '' -replace 'import javax\.inject\.Inject', '' -replace 'import javax\.inject\.Singleton', '' | Set-Content $repo

$pocketVm = "app/src/main/java/com/example/anonymouschat/feature_pocket/presentation/PocketViewModel.kt"
$content = Get-Content $pocketVm -Raw
$content = $content -replace '(?s)\r?\nimport androidx\.lifecycle\.ViewModelProvider\r?\nimport com\.example\.anonymouschat\.data\.DefaultDataRepository', ''
$content = "import androidx.lifecycle.ViewModelProvider`nimport com.example.anonymouschat.data.DefaultDataRepository`n" + $content
$content | Set-Content $pocketVm -NoNewline

Remove-Item -Path "app/src/main/java/com/example/anonymouschat/ui" -Recurse -Force -ErrorAction SilentlyContinue
