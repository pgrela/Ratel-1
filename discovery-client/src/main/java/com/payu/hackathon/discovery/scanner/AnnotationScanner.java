package com.payu.hackathon.discovery.scanner;

import static com.payu.hackathon.discovery.model.MethodBuilder.aMethod;
import static com.payu.hackathon.discovery.model.ServiceBuilder.aService;
import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.payu.hackathon.discovery.model.Service;

public class AnnotationScanner {
    private String packageToScan;

    private Reflections reflections;

    public AnnotationScanner(String packageToScan) {
        withPackage(packageToScan);
        reflections = buildReflection();

    }

    public Stream<Service> scan() {
        return scanClasses().stream().
                map(it ->
                        aService()
                                .withName(it.getName())
                                .withPath(findAnnotation(it, Path.class).value())
                                .withMethods(scanMethods(it))
                                .build());

    }

    private List<Service.Method> scanMethods(Class<?> aClass) {
        List<Service.Method> methods = scanMethods(aClass, GET.class);
        methods.addAll(scanMethods(aClass, POST.class));
        return methods;
    }
    private List<Service.Method> scanMethods(Class<?> aClass, Class<? extends java.lang.annotation.Annotation>
            annotation) {
        System.out.println("scaning: "+annotation);
        return getAllMethods(aClass, withModifier(Modifier.PUBLIC),
                withAnnotation(annotation)).stream()
                .map(it -> {
                    return aMethod()
                            .withName(it.getName())
                            .withMethodType(annotation)
                            .withPath(findAnnotation(it, Path.class) != null ? findAnnotation(it,
                                    Path.class).value() : "")
                            .build();

                })
                .collect(Collectors.toList());
    }

    protected Set<Class<?>> scanClasses() {
        return reflections.getTypesAnnotatedWith(Path.class);
    }

    private void withPackage(String packageToScan) {
        this.packageToScan = packageToScan;
    }



    private Reflections buildReflection() {

        return new Reflections(packageToScan, new TypeAnnotationsScanner(), new MethodAnnotationsScanner());

    }
}
