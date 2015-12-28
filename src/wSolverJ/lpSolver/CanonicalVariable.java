package wSolverJ.lpSolver;

/**
 * Variable
 * Created by Xavier on 15/12/15.
 */
class CanonicalVariable {
    final boolean isNotArtificial;

    CanonicalVariable(){
        isNotArtificial = true;
    }

    CanonicalVariable(boolean isNotArt){
        isNotArtificial = isNotArt;
    }
}
