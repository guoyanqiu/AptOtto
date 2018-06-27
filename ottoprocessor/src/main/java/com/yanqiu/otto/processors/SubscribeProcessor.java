package com.yanqiu.otto.processors;


import com.google.auto.service.AutoService;
import com.yanqiu.otto.bus.subscribe.Subscribe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 参考资料https://www.jianshu.com/p/07ef8ba80562
 * package com.example;    // PackageElement
 * <p>
 * public class Test {        // TypeElement
 * <p>
 * private int a;      // VariableElement
 * private Test test;  // VariableElement
 * <p>
 * public Test () {}    // ExecuteableElement
 * <p>
 * public void setA (  // ExecuteableElement
 * int newA   // TypeElement
 * ) {}
 * }
 * <p>
 * Created by mac on 18/6/25.
 *
 * @AutoService(Processor.class) 这是一个注解处理器，是Google开发的，用来生成META-INF/services/javax.annotation.processing.Processor文件的。
 */
@SupportedAnnotationTypes("com.yanqiu.otto.bus.subscribe.Subscribe")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public class SubscribeProcessor extends AbstractProcessor {
    private Messager mMessager;
    private Filer mFiler;
    private Types mTypes;

//用 @SupportedSourceVersion(SourceVersion.RELEASE_7) 代替
//    @Override
//    public SourceVersion getSupportedSourceVersion()
//    {
//        //支持的java版本
//        return SourceVersion.RELEASE_7;
//    }
//
    //用 @SupportedAnnotationTypes("com.yanqiu.otto.bus.subscribe.Subscribe")代替
//    @Override
//    public Set<String> getSupportedAnnotationTypes()
//    {
//        //支持的注解
//        Set<String> annotations = new LinkedHashSet<>();
//        annotations.add(Subscribe.class.getCanonicalName());
//        return annotations;
//    }

    /**
     * Found subscriber methods for a class (without superclasses).
     */
    private final Map<TypeElement, Set<ExecutableElement>> methodsByClass = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mTypes = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEv) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "开始扫描注解 ");
        //收集各个类中的@Subscribe方法
        collectSubscribesMethod(annotations, roundEv);

        //将收集到的信息动态创建
        createJavaFile();
        //return true代表退出处理SubscribeUtils类中
        return false;
    }

    private void collectSubscribesMethod(Set<? extends TypeElement> annotations, RoundEnvironment roundEv) {
        //遍历@Subscribe标注的元素
        for (Element annotatedElement : roundEv.getElementsAnnotatedWith(Subscribe.class)) {
            //如果是方法
            if (annotatedElement instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) annotatedElement;
                if (checkHasNoErrors(method)) {
                    //通过getEnclosingElement获取方法所在的类，比如MainActivity中sub1和sub2方法的classElement就是com.apt.otto.MainActivity
                    TypeElement classElement = (TypeElement) method.getEnclosingElement();
                    Set<ExecutableElement> methods = methodsByClass.get(classElement);
                    if (methods == null) {
                        methods = new HashSet<ExecutableElement>();
                        methodsByClass.put(classElement, methods);
                    }
                    methods.add(method);
                }
            }
        }//end for

    }

    //参数为methd的参数类型
    private String getClassString(TypeElement typeElement) {
        PackageElement packageElement = getPackageElement(typeElement);
        String packageString = packageElement.getQualifiedName().toString();
        String className = typeElement.getQualifiedName().toString();
        if (!packageString.isEmpty()) {
            if (packageString.equals("java.lang")) {
                className = typeElement.getSimpleName().toString();
            }
        }
        return className + ".class";
    }

    private PackageElement getPackageElement(TypeElement subscriberClass) {
        Element candidate = subscriberClass.getEnclosingElement();
        while (!(candidate instanceof PackageElement)) {
            candidate = candidate.getEnclosingElement();
        }
        return (PackageElement) candidate;
    }

    private void createJavaFile() {
        if (methodsByClass.isEmpty()) {
            return;
        }
        BufferedWriter writer = null;
        try {
            String packageName = "com.yanqiu.otto.auto";
            JavaFileObject sourceFile = mFiler.createSourceFile(packageName + ".SubscribeUtils");
            writer = new BufferedWriter(sourceFile.openWriter());
            writer.write("package " + packageName + ";\n");
            writer.write("import com.yanqiu.otto.bus.subscribe.SubscriberMethodInfo;\n");
            writer.write("import com.yanqiu.otto.bus.subscribe.SubscriberInfo;\n");
            writer.write("import com.yanqiu.otto.bus.SubscriberInfoFinder;\n");
            writer.write("import java.util.HashMap;\n");
            writer.write("import java.util.Map;\n\n");
            writer.write("/** This class is generated by OttBus, do not edit. */\n");
            writer.write("public class SubscribeUtils implements SubscriberInfoFinder{\n");
            writer.write("    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBERES;\n\n");
            writer.write("    static {\n");
            writer.write("        SUBSCRIBERES = new HashMap<Class<?>, SubscriberInfo>();\n\n");
            writeIndexLines(writer);
            writer.write("    }\n\n");
            writer.write("    private static void addSubscribeInfo(SubscriberInfo info) {\n");
            writer.write("        SUBSCRIBERES.put(info.getSubscriberClass(), info);\n");
            writer.write("    }\n\n");
            writer.write("    @Override\n");
            writer.write("    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {\n");
            writer.write("        SubscriberInfo info = SUBSCRIBERES.get(subscriberClass);\n");
            writer.write("        if (info != null) {\n");
            writer.write("            return info;\n");
            writer.write("        } else {\n");
            writer.write("            return null;\n");
            writer.write("        }\n");
            writer.write("    }\n");
            writer.write("}\n");


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            methodsByClass.clear();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //Silent
                }
            }
        }
    }

    private void writeIndexLines(BufferedWriter writer) throws IOException {
        for (TypeElement typeElement : methodsByClass.keySet()) {

            //com.apt.otto.MainActivity.class
            String subscribeHostClass = getClassString(typeElement);

            //执行addSubscribeInfo方法
            writeLine(writer, 2, "addSubscribeInfo(new SubscriberInfo(" + subscribeHostClass, ",new SubscriberMethodInfo[] {");
            int index = 0;


            //获取某类中添加了注解@SubScribe的方法集合
            Set<ExecutableElement> subscribeMethods = methodsByClass.get(typeElement);
            for (ExecutableElement method : subscribeMethods) {

                //获取@Subscribe的参数
                List<? extends VariableElement> parameters = method.getParameters();
                VariableElement param = parameters.get(0);

                //拿到方法参数的类型
                TypeMirror paramType = getParamTypeMirror(param);
                TypeElement paramElement = (TypeElement) mTypes.asElement(paramType);

                //方法名称
                String methodName = method.getSimpleName().toString();
                //获取参数的类型信息
                String paramClass = getClassString(paramElement);

                //构造SubscriberMethodInfo信息的字符串
                List<String> subscriberMethodInfoStrs = new ArrayList<>();

                //构建SubscriberMethodInfo对象
                subscriberMethodInfoStrs.add("new SubscriberMethodInfo" + "(\"" + methodName + "\",");
                index++;
                if (index != subscribeMethods.size()) {
                    subscriberMethodInfoStrs.add(paramClass + "),");
                } else {
                    subscriberMethodInfoStrs.add(paramClass + ")");
                }

                writeLine(writer, 3, subscriberMethodInfoStrs.toArray(new String[subscriberMethodInfoStrs.size()]));
                //com.apt.otto.Demo.class
                //String.class
                mMessager.printMessage(Diagnostic.Kind.NOTE, "paramClass＝＝" + paramClass);
            }

            writer.write("        }));\n\n");

        }
    }

    private void writeLine(BufferedWriter writer, int indentLevel, String... parts) throws IOException {
        writeLine(writer, indentLevel, 2, parts);
    }

    private void writeLine(BufferedWriter writer, int indentLevel, int indentLevelIncrease, String... parts)
            throws IOException {
        writeIndent(writer, indentLevel);
        int len = indentLevel * 4;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i != 0) {
                if (len + part.length() > 118) {
                    writer.write("\n");
                    if (indentLevel < 12) {
                        indentLevel += indentLevelIncrease;
                    }
                    writeIndent(writer, indentLevel);
                    len = indentLevel * 4;
                } else {
                    writer.write(" ");
                }
            }
            writer.write(part);
            len += part.length();
        }
        writer.write("\n");
    }

    private void writeIndent(BufferedWriter writer, int indentLevel) throws IOException {
        for (int i = 0; i < indentLevel; i++) {
            writer.write("    ");
        }
    }


    private TypeMirror getParamTypeMirror(VariableElement param) {
        TypeMirror typeMirror = param.asType();
        // Check for generic type
        if (typeMirror instanceof TypeVariable) {
            TypeMirror upperBound = ((TypeVariable) typeMirror).getUpperBound();
            if (upperBound instanceof DeclaredType) {
                mMessager.printMessage(Diagnostic.Kind.NOTE, "Using upper bound type " + upperBound +
                        " for generic parameter", param);
                typeMirror = upperBound;
            }
        }
        return typeMirror;
    }

    private boolean checkHasNoErrors(ExecutableElement method) {
        if (method.getModifiers().contains(Modifier.STATIC)) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must not be static", method);
            return false;
        }

        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must be public", method);
            return false;
        }

        List<? extends VariableElement> parameters = ((ExecutableElement) method).getParameters();
        if (parameters.size() != 1) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must have exactly 1 parameter", method);
            return false;
        }
        return true;
    }
}


