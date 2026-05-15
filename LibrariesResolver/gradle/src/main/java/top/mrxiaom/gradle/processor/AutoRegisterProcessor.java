package top.mrxiaom.gradle.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("top.mrxiaom.pluginbase.func.AutoRegister")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoRegisterProcessor extends AbstractProcessor {

    private TypeElement getAnnotation(Set<? extends TypeElement> annotations) {
        for (TypeElement annotation : annotations) {
            String name = annotation.getQualifiedName().toString();
            if (name.equals("top.mrxiaom.pluginbase.func.AutoRegister")) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement autoRegister = getAnnotation(annotations);
        if (autoRegister == null) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (Element element : roundEnv.getElementsAnnotatedWith(autoRegister)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                sb.append(typeElement.getQualifiedName()).append("\n");
            }
        }

        if (sb.length() > 0) {
            try {
                Writer writer = processingEnv.getFiler()
                        .createResource(
                                StandardLocation.CLASS_OUTPUT,
                                "",
                                "META-INF/PluginBaseHolders")
                        .openWriter();

                writer.write(sb.toString());
                writer.close();

            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                System.out.println(sw);
            }
        }

        return true;
    }
}
