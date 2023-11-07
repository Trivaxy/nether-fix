package xyz.trivaxy.netherfix;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NetherFixMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!targetClassName.equals("net.minecraft.src.game.level.NetherPortalHandler"))
            return;

        MethodNode target = targetClass.methods.stream().filter(node -> node.name.equals("useExistingPortal")).findFirst().orElse(null);

        if (target == null) {
            NetherFixMod.logger().severe("Failed to find target method! This should not happen");
            return;
        }

        int iSubOrdinal = -1;
        int ifICmpLeOrdinal = -1;
        int successCount = 0; // should be 3 if everything went well

        for (int i = 0; i < target.instructions.size(); i++) {
            AbstractInsnNode insn = target.instructions.get(i);
            int opcode = insn.getOpcode();

            switch (opcode) {
                case Opcodes.ISUB:
                    iSubOrdinal++;
                    if (iSubOrdinal == 2) {
                        target.instructions.set(insn, new InsnNode(Opcodes.IADD));
                        successCount++;
                    }
                break;
                case Opcodes.IF_ICMPLE:
                    ifICmpLeOrdinal++;
                    if (ifICmpLeOrdinal == 0) {
                        JumpInsnNode newInsn = new JumpInsnNode(Opcodes.IF_ICMPGT, ((JumpInsnNode) insn).label);
                        target.instructions.set(insn, newInsn);
                        target.instructions.set(newInsn.getPrevious(), new InsnNode(Opcodes.ISUB));
                        successCount++;
                    }
                break;
                case Opcodes.IINC:
                    IincInsnNode iincInsnNode = (IincInsnNode) insn;
                    if (iincInsnNode.var == 21 && iincInsnNode.incr == 3) {
                        iincInsnNode.incr = -3;
                        successCount++;
                    }
                break;
            }
        }

        if (successCount == 3)
            NetherFixMod.logger().info("Successfully patched nether portal bug");
        else
            NetherFixMod.logger().severe("Failed to patch! This should not happen. Please remove the mod to avoid unexpected crashes");
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
