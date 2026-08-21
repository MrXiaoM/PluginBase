setupJava(8)

dependencies {
    applyLibraries("spigot-api", "compileOnly", "testImplementation")

    applyLibraries("adventure", "compileOnly", "testCompileOnly")
    applyLibraries("nbt-api", "compileOnly")
}
