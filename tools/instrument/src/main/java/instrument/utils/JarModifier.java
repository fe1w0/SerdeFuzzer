package instrument.utils;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * take from http://noverguo.github.io/2019/09/10/fixjarbug/
 */

public class JarModifier {

    static String MAGIC_CODE = "SerdeFuzzer@fe1w0";

    public static void change(String inJarPath, String outJarPath, List<String> sinkMethods) throws IOException {
        List<ClassNode> classNodes = JarLoader.loadJar(inJarPath);
        List<ClassNode> changeNodes = new ArrayList<>();
        List<String> sinkClasses = getSinkClass(sinkMethods);

        for (ClassNode cn : classNodes) {
            if (sinkClasses.contains(cn.name)) {
                List<MethodNode> newMethods = new ArrayList<>();
                for (MethodNode mn : cn.methods) {
                    // System.out.println(cn.name + "#" + mn.name + "#" + mn.desc);
                    if (sinkMethods.contains((cn.name + "#" + mn.name + "#" + mn.desc))) {
                        // if (sinkMethods.get(0).contains(mn.name)) {
                        System.out.println("Instrument: " + cn.name + "\t" + mn.name + "\t" + mn.desc);
                        MethodNode newMethodNode = new MethodNode(mn.access, mn.name, mn.desc, mn.signature,
                                mn.exceptions.toArray(new String[0]));
                        mn.accept(new MethodVisitor(Opcodes.ASM8, newMethodNode) {

                            @Override
                            public void visitCode() {
                                newCode();
                            }

                            public void newCode() {
                                Logger.info("Instrument: {}.{} {}", cn.name, mn.name, mn.desc);
                                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                                        "Ljava/io/PrintStream;");
                                mv.visitLdcInsn(MAGIC_CODE);
                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
                                        "(Ljava/lang/String;)V", false);
                            }
                        });

                        printInstructions(newMethodNode.instructions);
                        newMethods.add(newMethodNode);
                    } else {
                        newMethods.add(mn);
                    }
                }
                cn.methods = newMethods;
                changeNodes.add(cn);
            }
        }
        JarLoader.saveToJar(inJarPath, outJarPath, changeNodes);
    }

    public static List<String> getSinkClass(List<String> sinks) {
        List<String> sinkClasses = new ArrayList<String>();
        for (String s : sinks) {
            String[] tmp = s.split("#");
            sinkClasses.add(tmp[0]);
        }
        return sinkClasses;
    }

    public static void printInstructions(InsnList instructions) {
        for (AbstractInsnNode instruction : instructions) {
            Logger.info(getInstructionText(instruction));
        }
    }

    private static String getInstructionText(AbstractInsnNode instruction) {
        if (instruction instanceof InsnNode) {
            return "InsnNode: " + getOpcodeName(instruction.getOpcode());
        } else if (instruction instanceof FieldInsnNode) {
            FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
            return "FieldInsnNode: " + getOpcodeName(instruction.getOpcode()) +
                    " owner=" + fieldInsn.owner +
                    " name=" + fieldInsn.name +
                    " desc=" + fieldInsn.desc;
        } else if (instruction instanceof MethodInsnNode) {
            MethodInsnNode methodInsn = (MethodInsnNode) instruction;
            return "MethodInsnNode: " + getOpcodeName(instruction.getOpcode()) +
                    " owner=" + methodInsn.owner +
                    " name=" + methodInsn.name +
                    " desc=" + methodInsn.desc +
                    " itf=" + methodInsn.itf;
        } else if (instruction instanceof LdcInsnNode) {
            return "LdcInsnNode: " + getOpcodeName(instruction.getOpcode()) +
                    " cst=" + ((LdcInsnNode) instruction).cst;
        } else {
            return instruction.toString();
        }
    }

    private static String getOpcodeName(int opcode) {
        return org.objectweb.asm.util.Printer.OPCODES[opcode];
    }
}
