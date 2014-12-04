package org.onelab.monitor.agent.transform.asm.inserter;

import org.objectweb.asm.MethodVisitor;

/**
 * 方法插入器，针对指定类的指定方法做切入，以某一方法的第n次调用标记切入点，
 * 切入点坐标 {owner，methodName，methodDesc,index}，注意：每一个切入
 * 点只允许对应一个方法插入器。
 * Created by chunliangh on 14-12-3.
 */
public abstract class CodeInserter {

    private final InsertPoint insertPoint;
    //校验前方法被调用的次数
    private int currIndex;

    public CodeInserter(InsertPoint insertPoint) {
        if (insertPoint == null) {
            throw new IllegalArgumentException();
        }
        this.insertPoint = insertPoint;
    }

    /**
     * 插入代码
     * @param mv
     * @param opcode
     * @param owner
     * @param name
     * @param desc
     * @param itf
     */
    public void insert(MethodVisitor mv,int opcode,String owner,String name,String desc,boolean itf){
        beforeMethodInsn(mv);
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        afterMethodInsn(mv);
    }

    /**
     * 在切入点之前插入代码
     * @param mv
     */
    abstract protected void beforeMethodInsn(MethodVisitor mv);

    /**
     * 在切入点之后插入代码
     * @param mv
     */
    abstract protected void afterMethodInsn(MethodVisitor mv);

    /**
     * 校验所调用方法的位置是否满足插入代码条件，如果满足返回true，否则返回false
     * @param owner
     * @param name
     * @param desc
     * @return
     */
    public boolean match(String owner,String name,String desc){
        if (!insertPoint.getPointType().equals(owner)
                || !insertPoint.getPointMethod().equals(name)
                || !insertPoint.getPointDesc().equals(desc)) {
            return false;
        }
        currIndex++;
        if (insertPoint.getPointIndex()==currIndex) return true;
        return false;
    }
}
