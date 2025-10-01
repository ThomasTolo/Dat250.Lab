import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "7.0.2"
}

node {
    // Use an LTS version to reduce risk of platform-specific packaging issues.
    version = "20.18.0"
    npmVersion = "10.8.2"
    download = true
}

// Clean task to remove downloaded Node if it becomes corrupted (e.g., broken symlink npx)
tasks.register<Delete>("cleanNodeArtifacts") {
    delete(file(".gradle/nodejs"))
}

tasks.register<NpmTask>("runBuild") {
    args = listOf("run", "build")
    workingDir = file(".")
}

// Copy built frontend into backend Spring Boot static resources.
// We place index.html & vite.svg at static/ root, and keep hashed assets under static/assets.
// Also clean old hashed asset files to avoid stale bundles piling up.
tasks.register<Copy>("copyWebApp") {
    dependsOn("runBuild")

    // Destination root for static resources
    val staticRoot = project.file("../src/main/resources/static")

    // First, delete old hashed asset files (simple glob on index-*.js / css) to prevent clutter.
    doFirst {
        val assetsDir = staticRoot.resolve("assets")
        if (assetsDir.exists()) {
            assetsDir.listFiles()?.forEach { f ->
                if (f.name.startsWith("index-") && (f.name.endsWith(".js") || f.name.endsWith(".css"))) {
                    f.delete()
                }
            }
        }
    }

    // Copy index.html & vite.svg to static root
    from("dist/index.html") { into(staticRoot) }
    from("dist/vite.svg") { into(staticRoot) }

    // Copy assets directory contents
    from("dist/assets") { into(staticRoot.resolve("assets")) }
}
