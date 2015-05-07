package com.aston.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 
 * @author pm
 *
 *         thanks to spring comunity for spring source code
 */
public class MethodParamNameParser {

	private static Map<Method, String[]> def = new HashMap<Method, String[]>();

	public static String[] params(Method m) {
		return m != null ? def.get(m) : null;
	}

	public static void prepareClass(Class<?> type, Class<? extends Annotation> filter) {

		try {
			final Map<String, Method> methods1 = new HashMap<String, Method>();
			for (Method m : type.getDeclaredMethods()) {
				if (m.getParameterTypes().length == 0)
					continue;
				if (filter != null && m.getAnnotation(filter) == null)
					continue;
				String sig = Type.getMethodDescriptor(m);
				methods1.put(m.getName() + "/" + sig, m);
			}

			ClassReader classReader = new ClassReader(type.getClassLoader().getResourceAsStream(type.getName().replace('.', File.separatorChar) + ".class"));
			classReader.accept(new EmptyClassVisitor() {

				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					Method m = methods1.get(name + "/" + desc);
					return m != null ? new LocalVariableTableVisitor(m, desc, isStatic(access)) : null;
				}

				private boolean isStatic(int access) {
					return ((access & Opcodes.ACC_STATIC) > 0);
				}
			}, 0);

		} catch (Exception e) {
			System.err.println("prepare class error " + type.getName() + " " + e.getMessage());
		}
	}

	static class LocalVariableTableVisitor extends EmptyMethodVisitor {

		boolean hasLvtInfo;
		Method m;
		int[] lvtSlotIndex;
		String[] parameterNames;

		LocalVariableTableVisitor(Method m, String desc, boolean isAstatic) {
			this.m = m;
			Type[] args = Type.getArgumentTypes(desc);
			this.lvtSlotIndex = computeLvtSlotIndices(isAstatic, args);
			this.hasLvtInfo = false;
			this.parameterNames = new String[args.length];
		}

		@Override
		public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
			this.hasLvtInfo = true;
			for (int i = 0; i < lvtSlotIndex.length; i++) {
				if (lvtSlotIndex[i] == index) {
					this.parameterNames[i] = name;
				}
			}
		}

		@Override
		public void visitEnd() {
			if (this.hasLvtInfo) {
				MethodParamNameParser.def.put(m, parameterNames);
			}
		}

		private int[] computeLvtSlotIndices(boolean isStatic, Type[] paramTypes) {
			int[] lvtIndex = new int[paramTypes.length];
			int nextIndex = (isStatic ? 0 : 1);
			for (int i = 0; i < paramTypes.length; i++) {
				lvtIndex[i] = nextIndex;
				if (isWideType(paramTypes[i])) {
					nextIndex += 2;
				} else {
					nextIndex++;
				}
			}
			return lvtIndex;
		}

		private boolean isWideType(Type aType) {
			// float is not a wide type
			return (aType == Type.LONG_TYPE || aType == Type.DOUBLE_TYPE);
		}
	}

	static class EmptyClassVisitor implements ClassVisitor {

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		}

		@Override
		public void visitSource(String source, String debug) {
		}

		@Override
		public void visitOuterClass(String owner, String name, String desc) {
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return null;
		}

		@Override
		public void visitAttribute(Attribute attr) {
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return null;
		}

		@Override
		public void visitEnd() {
		}

	}

	static class EmptyMethodVisitor implements MethodVisitor {

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return null;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return null;
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return null;
		}

		@Override
		public void visitAttribute(Attribute attr) {
		}

		@Override
		public void visitCode() {
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		}

		@Override
		public void visitInsn(int opcode) {
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
		}

		@Override
		public void visitLabel(Label label) {
		}

		@Override
		public void visitLdcInsn(Object cst) {
		}

		@Override
		public void visitIincInsn(int var, int increment) {
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		}

		@Override
		public void visitLineNumber(int line, Label start) {
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
		}

		@Override
		public void visitEnd() {
		}
	}
}
