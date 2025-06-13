package chicken.creaturecorner.server.entity.obj.base;

public interface IAnimatedEater {

    boolean isEating();

    void setEating(boolean eating);

    int eatingAnimationTimeout();

    void setEatingAnimationTimeout(int eatingAnimationTimeout);
}