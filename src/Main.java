import wSolverJ.canonicalSolver.CanonicalSolver;
import wSolverJ.canonicalSolver.CanonicalExpr;
import wSolverJ.canonicalSolver.Variable;
import wSolverJ.lpSolver.DVariable;
import wSolverJ.lpSolver.LpExpression;
import wSolverJ.lpSolver.LpSolver;

/**
 * Created by Xavier on 15/12/15.
 */
public class Main {

    public static void main(String[] args){
        //testLpSolver();
        //test2();
        testP1toP2();
    }

    static void testP1toP2(){
        LpSolver solver = new LpSolver();

        DVariable x1 = new DVariable();
        DVariable x2 = new DVariable();
        DVariable x3 = new DVariable();
        DVariable x4 = new DVariable();

        LpExpression obj = new LpExpression();
        obj.setElements(-3.0, x1);
        obj.setElements(1.0, x3);
        solver.setObjectiveMax(obj, 0.0);

        LpExpression exp = new LpExpression();
        exp.setElements(1.0, x1);
        exp.setElements(1.0, x2);
        exp.setElements(1.0, x3);
        exp.setElements(1.0, x4);
        solver.addEQ(exp, 4.0);

        exp = new LpExpression();
        exp.setElements(-2.0, x1);
        exp.setElements(1.0, x2);
        exp.setElements(-1.0, x3);
        solver.addEQ(exp, 1.0);

        exp = new LpExpression();
        exp.setElements(3.0, x2);
        exp.setElements(1.0, x3);
        exp.setElements(1.0, x4);
        solver.addEQ(exp, 9);

        exp = new LpExpression();
        exp.setElements(1.0, x1);
        solver.addGE(exp, 0.0);

        exp = new LpExpression();
        exp.setElements(1.0, x2);
        solver.addGE(exp, 0.0);

        exp = new LpExpression();
        exp.setElements(1.0, x3);
        solver.addGE(exp, 0.0);

        exp = new LpExpression();
        exp.setElements(1.0, x4);
        solver.addGE(exp, 0.0);

        solver.solve();
    }

    static void testP1toP2_d(){
        CanonicalSolver solver = new CanonicalSolver();
        Variable x1 = new Variable();
        Variable x2 = new Variable();
        Variable x3 = new Variable();
        Variable x4 = new Variable(false);
        Variable x5 = new Variable(false);
        Variable x6 = new Variable(false);
        double a = 1;

        CanonicalExpr obj = new CanonicalExpr();
        obj.setElement(3.0, x1);
        obj.setElement(-6.0, x2);
        obj.setElement(a-1.0, x3);
        solver.setObjective(1, obj, -6);

        CanonicalExpr exp = new CanonicalExpr();
        exp.setElement(1.0, x1);
        exp.setElement(-2.0, x2);
        exp.setElement(1.0, x4);
        solver.addConstraint(exp, 2.0, x4);

        exp = new CanonicalExpr();
        exp.setElement(1.0, x1);
        exp.setElement(-3.0, x2);
        exp.setElement(-1.0, x3);
        exp.setElement(1.0, x5);
        solver.addConstraint(exp, 1, x5);

        exp = new CanonicalExpr();
        exp.setElement(1.0, x1);
        exp.setElement(-1.0, x2);
        exp.setElement(a, x3);
        exp.setElement(1.0, x6);
        solver.addConstraint(exp, 3.0, x6);

        solver.solve(1);
        solver.removeArtificial();
    }

    static void test2(){
        LpSolver lpSolver = new LpSolver();
        DVariable x1 = new DVariable();
        DVariable x2 = new DVariable();
        DVariable x3 = new DVariable();

        LpExpression obj = new LpExpression();
        obj.setElements(6.0, x1);
        obj.setElements(14.0, x2);
        obj.setElements(13.0, x3);
        lpSolver.setObjectiveMax(obj, 0.0);

        LpExpression expr = new LpExpression();
        expr.setElements(0.5, x1);
        expr.setElements(2.0, x2);
        expr.setElements(1.0, x3);
        lpSolver.addLE(expr, 24.0);

        expr = new LpExpression();
        expr.setElements(1.0, x1);
        expr.setElements(2.0, x2);
        expr.setElements(4.0, x3);
        lpSolver.addLE(expr, 60.0);

        expr = new LpExpression();
        expr.setElements(1.0, x1);
        lpSolver.addGE(expr, 0);
        expr = new LpExpression();
        expr.setElements(1.0, x2);
        lpSolver.addGE(expr, 0);
        expr = new LpExpression();
        expr.setElements(1.0, x3);
        lpSolver.addGE(expr, 0);

        lpSolver.solve();
    }

    static void testLpSolver(){
        LpSolver lpSolver = new LpSolver();
        DVariable y1 = new DVariable();
        DVariable y2 = new DVariable();
        DVariable y3 = new DVariable();
        DVariable y4 = new DVariable();

        LpExpression obj = new LpExpression();
        obj.setElements(-3.0, y1);
        obj.setElements(2.0, y2);
        obj.setElements(-1.0, y3);
        obj.setElements(4.0, y4);
        lpSolver.setObjectiveMax(obj, 0.0);

        LpExpression expr = new LpExpression();
        expr.setElements(1.0, y1);
        expr.setElements(1.0, y2);
        expr.setElements(-4.0, y3);
        expr.setElements(2.0, y4);
        lpSolver.addGE(expr, 4.0);

        expr = new LpExpression();
        expr.setElements(-3.0, y1);
        expr.setElements(1.0, y2);
        expr.setElements(-2.0, y3);
        lpSolver.addLE(expr, 6);

        expr = new LpExpression();
        expr.setElements(1.0, y2);
        expr.setElements(-1.0, y4);
        lpSolver.addEQ(expr, -1);

        expr = new LpExpression();
        expr.setElements(1.0, y1);
        expr.setElements(1.0, y2);
        expr.setElements(-1.0, y3);
        lpSolver.addEQ(expr, 0.0);

        expr = new LpExpression();
        expr.setElements(1.0, y3);
        lpSolver.addGE(expr, 0.0);
        expr = new LpExpression();
        expr.setElements(1.0, y4);
        lpSolver.addGE(expr, 0.0);

        lpSolver.solve();
    }


}
