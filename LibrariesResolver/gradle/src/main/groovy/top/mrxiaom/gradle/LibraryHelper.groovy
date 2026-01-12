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
import java.util.function.Function

class LibraryHelper {
    private final List<String> resolvedLibraries = new ArrayList<>()
    private Project project
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
        List<ResolvedDependency> list = new ArrayList<>()
        for (final def dependency in configuration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            collect(list, dependency)
        }
        return list.collect {
            collector.apply(it)
        }
    }

    List<String> doResolveLibraries() {
        return doResolveLibraries(defaultCollector())
    }

    List<String> doResolveLibraries(Function<ResolvedDependency, String> collector) {
        resolvedLibraries.clear()
        resolvedLibraries.addAll(collectLibraries(collector))
        return resolvedLibraries
    }
}
