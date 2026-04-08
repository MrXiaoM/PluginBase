package top.mrxiaom.gradle

import com.google.common.collect.Iterables
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer
import java.util.function.Function

class LibraryHelper {
    private final List<String> resolvedLibraries = new ArrayList<>()
    private Project project
    private List<String> addedLibraries = new ArrayList<>();
    /**
     * 收集依赖用的配置
     */
    private Configuration configuration
    /**
     * 添加依赖的目标配置，例如 compileOnly、implementation
     */
    private String targetConfiguration
    /**
     * 当前版本的 LibrariesResolver 依赖简要声明字符串
     */
    public final LibrariesResolver resolver = new LibrariesResolver()
    /**
     * 当前版本的 PluginBase 各模块依赖简要声明字符串
     */
    public final PluginBase modules = new PluginBase()
    /**
     * 当前 PluginBase 推荐使用的依赖的简要声明字符串
     */
    public final Depend depend = new Depend()
    LibraryHelper(@NotNull Project project) {
        this(project, project.getConfigurations().create("libraryGroup", it -> {
            it.setCanBeResolved(true)
            it.setCanBeConsumed(false)
        }))
    }
    LibraryHelper(@NotNull Project project, @NotNull Configuration configuration) {
        this(project, configuration, "compileOnly")
    }
    LibraryHelper(@NotNull Project project, @NotNull Configuration configuration, @Nullable String targetConfiguration) {
        this.project = project
        this.configuration = configuration
        this.targetConfiguration = targetConfiguration
    }

    void library(String dependencyNotation) {
        addedLibraries.add(dependencyNotation)
        def dependencies = project.getDependencies()
        dependencies.add(configuration.getName(), dependencyNotation)
        if (targetConfiguration != null) {
            dependencies.add(targetConfiguration, dependencyNotation)
        }
    }

    void library(String dependencyNotation, Consumer<ExternalModuleDependency> consumer) {
        addedLibraries.add(dependencyNotation)
        def dependencies = project.getDependencies()
        dependencies.add(configuration.getName(), dependencyNotation, toClosure(consumer))
        if (targetConfiguration != null) {
            dependencies.add(targetConfiguration, dependencyNotation, toClosure(consumer))
        }
    }

    private static Closure toClosure(Consumer consumer) {
        return {
            def t -> consumer.accept(t)
        }
    }

    @NotNull
    Project getProject() {
        return project
    }

    void setProject(@NotNull Project project) {
        this.project = project
    }

    @NotNull
    Configuration getConfiguration() {
        return configuration
    }

    void setConfiguration(@NotNull Configuration configuration) {
        this.configuration = configuration
    }

    @Nullable
    String getTargetConfiguration() {
        return targetConfiguration
    }

    void setTargetConfiguration(@Nullable String targetConfiguration) {
        this.targetConfiguration = targetConfiguration
    }

    List<String> getResolvedLibraries() {
        return resolvedLibraries
    }

    static Function<ResolvedDependency, String> defaultCollector() {
        return (dep) -> {
            ResolvedArtifact artifact = Iterables.getFirst(dep.getModuleArtifacts(), null)
            if (artifact == null) return null
            String classifier = artifact.getClassifier()
            String extension = artifact.getExtension() ?: "jar"
            // 构建依赖文件路径
            StringBuilder sb = new StringBuilder()
            sb.append(dep.getModuleGroup().replace('.', '/'))
            sb.append('/')
            sb.append(dep.getModuleName())
            sb.append('/')
            sb.append(dep.getModuleVersion())
            sb.append('/')
            sb.append(dep.getModuleName())
            sb.append('-')
            sb.append(dep.getModuleVersion())
            if (classifier != null) {
                sb.append('-')
                sb.append(classifier)
            }
            sb.append('.')
            sb.append(extension)
            return sb.toString()
        }
    }

    String join() {
        return join(defaultCollector())
    }

    String join(Function<ResolvedDependency, String> collector) {
        if (resolvedLibraries.isEmpty()) {
            doResolveLibraries(collector)
        }
        def joiner = new StringJoiner(",\n")
        for (final def s in resolvedLibraries) {
            joiner.add("    \"$s\"")
        }
        return "new String[] {\n$joiner\n}"
    }

    private <T> void collect(List<ResolvedDependency> resolvedList, ResolvedDependency dep) {
        if (dep == null) return
        if (!resolvedList.contains(dep)) {
            resolvedList.add(dep)
            for (final def child in dep.getChildren()) {
                collect(resolvedList, child)
            }
        }
    }

    <T> List<T> collectLibraries(Function<ResolvedDependency, T> collector) {
        List<ResolvedDependency> depList = new ArrayList<>()
        for (final def dependency in configuration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            collect(depList, dependency)
        }
        Map<String, ResolvedDependency> depMap = new HashMap<>()
        for (final def dep in depList) {
            String key = dep.moduleGroup + ":" + dep.moduleName
            def old = depMap.get(key)
            if (old == null || dep.moduleVersion > old.moduleVersion) {
                depMap.put(key, dep)
            }
        }
        List<T> list = new ArrayList()
        for (final def dep in depMap.values()) {
            T entry = collector.apply(dep)
            if (entry != null) {
                list.add(entry)
            }
        }
        return list
    }

    List<String> doResolveLibraries() {
        return doResolveLibraries(defaultCollector())
    }

    List<String> doResolveLibraries(Function<ResolvedDependency, String> collector) {
        resolvedLibraries.clear()
        resolvedLibraries.addAll(collectLibraries(collector))
        return resolvedLibraries
    }

    List<String> getAddedLibraries() {
        return Collections.unmodifiableList(addedLibraries)
    }

    List<String> getAddedLibrariesYAML() {
        List<String> list = new ArrayList<>()
        list.add("")
        if (addedLibraries.isEmpty()) {
            list.add("libraries: []")
        } else {
            list.add("libraries:")
            for (final def lib in addedLibraries) {
                list.add("  - \"" + lib + "\"")
            }
        }
        return list
    }

    static void initJava(Project project, LibraryHelper base, int targetJavaVersion, boolean extraJar) {
        project.extensions.configure(JavaPluginExtension.class) {
            it.disableAutoTargetJvm()
            def javaVersion = JavaVersion.toVersion(targetJavaVersion)
            if (JavaVersion.current() < javaVersion) {
                it.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
            }
            if (extraJar) {
                it.withJavadocJar()
                it.withSourcesJar()
            }
        }
        def tasks = project.tasks
        def shadowJar = tasks.named("shadowJar")

        def copyTask = tasks.register("copyBuildArtifact", Copy.class) {
            it.dependsOn(shadowJar)
            it.from(shadowJar.get().outputs)
            it.rename((_) -> "${project.name}-${project.version}.jar")
            it.into(project.rootProject.file("out"))
        }
        tasks.named("build") {
            it.dependsOn(copyTask)
        }
        if (extraJar) {
            tasks.named("javadoc", Javadoc.class) {
                def options = it.options as StandardJavadocDocletOptions

                options.links("https://hub.spigotmc.org/javadocs/spigot/")

                options.locale("zh_CN")
                options.encoding("UTF-8")
                options.docEncoding("UTF-8")
                options.addBooleanOption("keywords", true)
                options.addBooleanOption("Xdoclint:none", true)
            }
        }
        tasks.withType(JavaCompile.class).configureEach {
            it.options.encoding = "UTF-8"
            it.options.compilerArgs.add("-Xlint:-options")
            if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
                it.options.release.set(targetJavaVersion)
            }
        }
        def sourceSets = (SourceSetContainer) project.extensions.getByName("sourceSets")
        tasks.named("processResources", ProcessResources.class) {
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            it.from(sourceSets.named("main").get().resources.srcDirs) { CopySpec copy ->
                Map<String, Object> map = new HashMap<>()
                map.put("version", project.version)
                if (base != null) {
                    def joiner = new StringJoiner("\"\n  - \"")
                    for (final def lib in base.getAddedLibraries()) {
                        joiner.add(lib)
                    }
                    map.put("libraries", joiner.toString())
                    map.put("libraries_config", base.getAddedLibrariesYAML().join("\n"))
                }
                copy.expand(map)
                copy.include("plugin.yml")
            }
        }
    }

    static void initPublishing(Project project) {
        project.extensions.configure(PublishingExtension.class) {
            it.publications {
                it.create("maven", MavenPublication.class) {
                    it.groupId = project.group.toString()
                    it.artifactId = project.name
                    it.version = project.version.toString()

                    it.artifact(project.tasks.named("shadowJar")).classifier = null
                    it.artifact(project.tasks.named("sourcesJar"))
                    it.artifact(project.tasks.named("javadocJar"))
                }
            }
        }
    }
}
