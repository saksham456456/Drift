$appBuild = "app/build.gradle.kts"
(Get-Content $appBuild) -replace 'alias\(libs\.plugins\.hilt\)', '' -replace 'alias\(libs\.plugins\.ksp\)', '' -replace 'ksp\(libs\.hilt\.compiler\)', '' -replace 'implementation\(libs\.hilt\.android\)', '' -replace 'implementation\(libs\.androidx\.hilt\.navigation\.compose\)', '' | Set-Content $appBuild

$rootBuild = "build.gradle.kts"
(Get-Content $rootBuild) -replace 'alias\(libs\.plugins\.ksp\).*', '' -replace 'alias\(libs\.plugins\.hilt\).*', '' | Set-Content $rootBuild

$chatApp = "app/src/main/java/com/example/anonymouschat/ChatApp.kt"
(Get-Content $chatApp) -replace '@HiltAndroidApp', '' -replace 'import dagger\.hilt\.android\.HiltAndroidApp', '' | Set-Content $chatApp

$mainAct = "app/src/main/java/com/example/anonymouschat/MainActivity.kt"
(Get-Content $mainAct) -replace '@AndroidEntryPoint', '' -replace 'import dagger\.hilt\.android\.AndroidEntryPoint', '' | Set-Content $mainAct

$oceanVm = "app/src/main/java/com/example/anonymouschat/feature_ocean/presentation/OceanViewModel.kt"
(Get-Content $oceanVm) -replace '@HiltViewModel', '' -replace '@Inject constructor', '' -replace 'import dagger\.hilt\.android\.lifecycle\.HiltViewModel', '' -replace 'import javax\.inject\.Inject', '' | Set-Content $oceanVm

$pocketVm = "app/src/main/java/com/example/anonymouschat/feature_pocket/presentation/PocketViewModel.kt"
(Get-Content $pocketVm) -replace '@HiltViewModel', '' -replace '@Inject constructor', '' -replace 'import dagger\.hilt\.android\.lifecycle\.HiltViewModel', '' -replace 'import javax\.inject\.Inject', '' | Set-Content $pocketVm
