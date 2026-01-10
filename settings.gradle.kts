// TODO: change this to your plugin name
rootProject.name = "NekoTerraForged"
pluginManagement {
    repositories {
        maven("https://repo.leavesmc.org/releases") {
            name = "leavesmc-releases"
        }
        maven("https://repo.leavesmc.org/snapshots") {
            name = "leavesmc-snapshots"
        }
        gradlePluginPortal()
    }
}