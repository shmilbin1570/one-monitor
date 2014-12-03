package org.onelab.monitor.agent.transform.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.onelab.monitor.agent.config.Commons;
import org.onelab.monitor.agent.transform.TransformedMethod;
import org.onelab.monitor.agent.transform.asm.inserter.CodeInserter;
import org.onelab.monitor.agent.transform.asm.inserter.CodeInserterPool;

import java.util.List;

/**
 * monitor-agent方法适配器
 * Created by chunliangh on 14-11-14.
 */
public class AgentMethodAdapter extends AdviceAdapter implements Opcodes, Commons {

    private String className;
    private String methodName;
    private String methodDesc;
    private int processFlag;

    private boolean hasTransformedMethod = false;

    private Label startFinally = new Label();

    public AgentMethodAdapter(int processFlag, String className, final MethodVisitor mv,
                              final int access, final String methodName, final String methodDesc) {
        super(ASM4, mv, access, methodName, methodDesc);
        this.processFlag = processFlag;
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
//        CodeInserterPool.addMethodCodeInserter("com/jumei/pic/demo/controller/PicController", "upload", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;Lorg/springframework/web/multipart/commons/CommonsMultipartFile;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
//                new CodeInserter("com/jumei/pic/utils/PicUtils", "upLoadPic", "(Ljava/lang/String;Lcom/jumei/pic/utils/Config;[B)Ljava/lang/String;", 1) {
//                    @Override
//                    protected void insertBefore(MethodVisitor mv) {
//                        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//                        mv.visitLdcInsn("************************************************************");
//                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//                    }
//
//                    @Override
//                    protected void insertAfter(MethodVisitor mv) {
//                        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//                        mv.visitLdcInsn("************************************************************");
//                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//                    }
//                }
//        );
    }

    @Override
    protected void onMethodEnter() {
        super.visitLdcInsn(processFlag);
        super.visitLdcInsn(className);
        super.visitLdcInsn(methodName);
        super.visitLdcInsn(methodDesc);
        if ((methodAccess & Opcodes.ACC_STATIC) != 0) {
            super.visitInsn(ACONST_NULL);
        } else {
            super.loadThis();
        }
        super.loadArgArray();
        super.visitMethodInsn(Opcodes.INVOKESTATIC, AGENT_HANDLER_CLASS, AGENT_HANDLER_ENTER, AGENT_HANDLER_ENTER_DESC, false);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != Opcodes.ATHROW) {
            exitNormally(opcode);
        }
    }

    private void exitWithException() {
        super.dup();
        Type thType = Type.getType(Throwable.class);
        int th = newLocal(thType);
        storeLocal(th);
        loadLocal(th);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, AGENT_HANDLER_CLASS, AGENT_HANDLER_FAIL, AGENT_HANDLER_FAIL_DESC, false);
    }

    private void exitNormally(int opcode) {
        if (opcode == RETURN) {
            visitInsn(ACONST_NULL);
        } else if (opcode == ARETURN || opcode == ATHROW) {
            super.dup();
        } else {
            if (opcode == LRETURN || opcode == DRETURN) {
                super.dup2();
            } else {
                super.dup();
            }
            super.box(Type.getReturnType(this.methodDesc));
        }
        super.visitMethodInsn(Opcodes.INVOKESTATIC, AGENT_HANDLER_CLASS, AGENT_HANDLER_EXIT, AGENT_HANDLER_EXIT_DESC, false);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        super.visitLabel(this.startFinally);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.contains(Commons.AGENT_TRANSFORMED_METHOD)){
            hasTransformedMethod = true;
        }
        return super.visitAnnotation(desc,visible);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        List<CodeInserter> codeInserters = CodeInserterPool.get(className, methodName, methodDesc);
        if (codeInserters != null){
            for (CodeInserter codeInserter:codeInserters){
                codeInserter.visit(mv,opcode,owner,name,desc,itf);
            }
        }else{
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label endFinally = new Label();
        super.visitTryCatchBlock(this.startFinally, endFinally, endFinally, null);
        super.visitLabel(endFinally);
        exitWithException();
        super.visitInsn(Opcodes.ATHROW);
        super.visitMaxs(0, 0);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (!hasTransformedMethod){
            super.visitAnnotation(Type.getDescriptor(TransformedMethod.class), true);
        }
    }
}
