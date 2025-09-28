Command for windows powershell to package the fat jar to exe installer
jpackage `
   --name Avtodiva `
   --input target `
   --main-jar avtodiva-0.0.1-SNAPSHOT.jar `
   --main-class org.springframework.boot.loader.launch.JarLauncher `
   --type exe `
   --icon src/main/resources/avtodiva_logo.ico `
   --win-menu `
   --win-shortcut `
   --win-dir-chooser `
   --runtime-image "$env:JAVA_HOME"