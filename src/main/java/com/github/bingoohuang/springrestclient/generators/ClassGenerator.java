package com.github.bingoohuang.springrestclient.generators;

import com.github.bingoohuang.springrestclient.annotations.SpringRestClientEnabled;
import com.github.bingoohuang.springrestclient.annotations.SuccInResponseJSONProperty;
import com.github.bingoohuang.springrestclient.provider.BaseUrlProvider;
import com.github.bingoohuang.springrestclient.provider.BasicAuthProvider;
import com.github.bingoohuang.springrestclient.provider.SignProvider;
import com.github.bingoohuang.springrestclient.utils.Obj;
import com.google.common.io.Files;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.github.bingoohuang.springrestclient.generators.MethodGenerator.*;
import static com.github.bingoohuang.springrestclient.utils.Asms.ci;
import static com.github.bingoohuang.springrestclient.utils.Asms.p;
import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator<T> {
    private final Class<T> restClientClass;
    private final String implName;
    private final ClassWriter cw;
    private final SpringRestClientEnabled clientEnabled;

    public ClassGenerator(Class<T> restClientClass) {
        this.restClientClass = restClientClass;
        this.implName = restClientClass.getName() + "$$BINGOOASM$$Impl";
        this.cw = createClassWriter();
        this.clientEnabled = restClientClass.getAnnotation(SpringRestClientEnabled.class);
    }

    public Class<? extends T> generate() {
        val bytes = createImplClassBytes();

        createClassFileForDiagnose(bytes);

        return defineClass(bytes);
    }

    public String getClassRequestMapping() {
        val re = restClientClass.getAnnotation(RequestMapping.class);
        return re != null && re.value().length > 0 ? re.value()[0] : "";
    }

    private void createClassFileForDiagnose(byte[] bytes) {
        if (clientEnabled.createClassFileForDiagnose())
            writeClassFile4Diagnose(bytes, implName + ".class");
    }

    private void writeClassFile4Diagnose(byte[] bytes, String fileName) {
        try {
            Files.write(bytes, new File(fileName));
        } catch (IOException e) {
            // ignore
        }
    }

    private Class<? extends T> defineClass(byte[] bytes) {
        val parentClassLoader = restClientClass.getClassLoader();
        val classLoader = new RestClientClassLoader(parentClassLoader);
        return (Class<? extends T>) classLoader.defineClass(implName, bytes);
    }

    private byte[] createImplClassBytes() {
        val classRequestMapping = getClassRequestMapping();

        constructor();

        for (val method : restClientClass.getMethods()) {
            new MethodGenerator(cw, implName, method, classRequestMapping).generate();
        }

        return createBytes();
    }

    private byte[] createBytes() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    private ClassWriter createClassWriter() {
        val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, implName.replace('.', '/'),
                null, p(Object.class), new String[]{Type.getInternalName(restClientClass)});

        cw.visitField(0, baseUrlProvider, ci(BaseUrlProvider.class), null, null).visitEnd();
        cw.visitField(0, basicAuthProvider, ci(BasicAuthProvider.class), null, null).visitEnd();
        cw.visitField(0, signProvider, ci(SignProvider.class), null, null).visitEnd();
        cw.visitField(0, appContext, ci(ApplicationContext.class), null, null).visitEnd();

        for (val method : restClientClass.getDeclaredMethods()) {
            val prefix = Obj.getMethodNamePrefixWithHashCode(method);

            cw.visitField(0, prefix + StatusExceptionMappings, ci(Map.class), null, null).visitEnd();
            cw.visitField(0, prefix + FixedRequestParams, ci(Map.class), null, null).visitEnd();
            cw.visitField(0, prefix + SuccInResponseJSONProperty, ci(SuccInResponseJSONProperty.class), null, null).visitEnd();
        }

        return cw;
    }

    private void constructor() {
        val mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, p(Object.class), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
