package wSolverJ.lpSolver;

/**
 * Linear program constraint
 * Created by Xavier on 15/12/16.
 */
class LpConstraint {
    enum  CType{
        EQ, LE, GE
    }

    LpExpression lpExpr;
    CType type;
    Double constant;

    LpConstraint(LpExpression exp, CType ct, Double c){
        lpExpr = exp;
        type = ct;
        constant = c;
    }
}
