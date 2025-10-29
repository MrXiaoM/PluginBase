package top.mrxiaom.gradle

import com.google.common.collect.Iterables
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.util.function.Consumer

class LibraryHelper {
    private final List<String> resolvedLibraries = new ArrayList<>()
    private Project project
    private Configuration configuration
    private String targetConfiguration
    public final LibrariesResolver resolver = new LibrariesResolver()
    public final PluginBase modules = new PluginBase()
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
        def dependencies = project.getDependencies()
        dependencies.add(configuration.getName(), dependencyNotation)
        if (targetConfiguration != null) {
            dependencies.add(targetConfiguration, dependencyNotation)
        }
    }

    void library(String dependencyNotation, Consumer<ExternalModuleDependency> consumer) {
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

    String join() {
        if (resolvedLibraries.isEmpty()) {
            doResolveLibraries()
        }
        def joiner = new StringJoiner(",\n")
        for (final def s in resolvedLibraries) {
            joiner.add("    \"$s\"")
        }
        return "new String[] {\n$joiner\n}"
    }

    private void collect(ResolvedDependency dep) {
        ResolvedArtifact artifact = Iterables.getFirst(dep.getModuleArtifacts(), null)
        if (artifact == null) return
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
        String s = sb.toString()
        if (!resolvedLibraries.contains(s)) {
            resolvedLibraries.add(s)
            dep.getChildren().forEach(this::collect)
        }
    }
    List<String> doResolveLibraries() {
        configuration.getResolvedConfiguration().getFirstLevelModuleDependencies().forEach(this::collect)
        return resolvedLibraries
    }
}
