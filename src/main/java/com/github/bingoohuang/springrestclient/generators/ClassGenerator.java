package com.github.bingoohuang.springrestclient.generators;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator<T> {
    private final Class<T> restClientClass;
    private final String implName;
    private final ClassWriter classWriter;

    public ClassGenerator(Class<T> restClientClass) {
        this.restClientClass = restClientClass;
        this.implName = restClientClass.getName() + "Impl";
        this.classWriter = createClassWriter();
    }

    public Class<? extends T> generate() {
        byte[] bytes = createRestClientImplClassBytes();

        diagnose(bytes);

        return defineClass(bytes);
    }

    private void diagnose(byte[] bytes) {
        writeClassFile4Diagnose(bytes, restClientClass.getSimpleName() + "Impl.class");
    }

    private void writeClassFile4Diagnose(byte[] bytes, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Class<? extends T> defineClass(byte[] bytes) {
        return (Class<? extends T>) new RestClientClassLoader(restClientClass.getClassLoader()).defineClass(implName, bytes);
    }

    private byte[] createRestClientImplClassBytes() {
        constructor();

        for (Method method : restClientClass.getMethods()) {
            new MethodGenerator(method, classWriter).genereate();
        }

        return createBytes();
    }

    private byte[] createBytes() {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private ClassWriter createClassWriter() {
        final String implSourceName = implName.replace('.', '/');
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String[] interfaces = {Type.getInternalName(restClientClass)};
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, implSourceName, null, "java/lang/Object", interfaces);
        return cw;
    }

    private void constructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}