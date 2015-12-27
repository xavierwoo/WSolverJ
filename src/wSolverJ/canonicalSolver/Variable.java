package wSolverJ.canonicalSolver;

/**
 * Variable
 * Created by Xavier on 15/12/15.
 */
public class Variable {
//    static public enum Type{
//        NORMAL, SLACK, ARTIFICIAL
//    }

    public final boolean isNotArtificial;

    public Variable(){
        isNotArtificial = true;
    }

    public Variable(boolean isNotArt){
        isNotArtificial = isNotArt;
    }
}
