package wSolverJ.canonicalSolver;

/**
 * Variable
 * Created by Xavier on 15/12/15.
 */
public class Variable {
    public final boolean isNotArtificial;

    public Variable(){
        isNotArtificial = true;
    }

    public Variable(boolean isNotArt){
        isNotArtificial = isNotArt;
    }
}
