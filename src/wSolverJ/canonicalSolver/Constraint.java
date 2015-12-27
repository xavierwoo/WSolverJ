package wSolverJ.canonicalSolver;

/**
 * linear Constraint
 * Created by Xavier on 15/12/15.
 */
public class Constraint {

    public CanonicalExpr lExpression;

    public double rConstant;

    public Variable basic;

    public Constraint(CanonicalExpr exp, double cnst, Variable basicVar){
        lExpression = exp;

        rConstant = cnst;

        basic = basicVar;
    }

}
