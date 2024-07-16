tasks.register<Exec>("run") {
    group = "spikedog"
    dependsOn("jar")
    doFirst {
        if (!file("run").exists()) {
            file("run").mkdir()
        }
        if (!file("run/Spikedog.jar").exists()) {
            val url = "https://github.com/Diruptio/Spikedog/releases/latest/download/Spikedog.jar"
            ant.invokeMethod("get", mapOf("src" to url, "dest" to file("run/Spikedog.jar")))
        }
        val output = (tasks.getByName("jar") as Jar).outputs.files.first()
        file("run/modules/${output.name}").delete()
        output.copyTo(file("run/modules/${output.name}"))
    }
    workingDir = file("run")
    standardOutput = System.out
    standardInput = System.`in`
    commandLine("java", "-jar", "Spikedog.jar")
}
