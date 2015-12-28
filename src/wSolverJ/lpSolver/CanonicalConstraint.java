package wSolverJ.lpSolver;

/**
 * linear Constraint
 * Created by Xavier on 15/12/15.
 */
class CanonicalConstraint {

    CanonicalExpr lExpression;

    double rConstant;

    CanonicalVariable basic;

    CanonicalConstraint(CanonicalExpr exp, double cnst, CanonicalVariable basicVar){
        lExpression = exp;

        rConstant = cnst;

        basic = basicVar;
    }

}
