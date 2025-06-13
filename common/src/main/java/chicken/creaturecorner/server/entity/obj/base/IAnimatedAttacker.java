package chicken.creaturecorner.server.entity.obj.base;

public interface IAnimatedAttacker {

    boolean isAttacking();

    void setAttacking(boolean attacking);

    int attackAnimationTimeout();

    void setAttackAnimationTimeout(int attackAnimationTimeout);
}