package pascal.taie.analysis.pta.plugin.myreflection;

import pascal.taie.analysis.graph.callgraph.Edge;
import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.*;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.plugin.Plugin;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.classes.*;
import pascal.taie.language.type.Type;

import java.util.HashMap;
import java.util.Map;

public class MyReflectionAnalysis implements Plugin {
    private Solver solver;
    private CSManager csManager;
    private Context emptyContext;
    private Map<Var,JClass> class_pts=new HashMap<>();

    @Override
    public void setSolver(Solver solver){
        this.solver=solver;
        csManager= solver.getCSManager();
        emptyContext=solver.getContextSelector().getEmptyContext();
    }
    @Override
    public void onNewMethod(JMethod method){
        for(Var var:method.getIR().getVars()){
            for(Invoke invoke: var.getInvokes()){
                onProcessCall(var,invoke);
            }
        }
    }
    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts){

    }
    @Override
    public void onNewCSMethod(CSMethod csMethod){

    }
    @Override
    public void onNewCallEdge(Edge<CSCallSite, CSMethod> edge){

    }
    @Override
    public void onProcessInvokeStatic(Invoke invoke){
        //System.out.println("********************"+invoke.getInvokeExp().getMethodRef().toString());
        String[] method=invoke.getMethodRef().toString().split("\\s+");
        if(method[1].equals("java.lang.Class")){
            String[] name=method[2].split("\\(");
            if(name[0].equals("forName")||name[0].equals("class$"))//case 1: forName
            {
                //System.out.println("********************"+invoke.getInvokeExp().getMethodRef().toString());
                Var c=invoke.getResult();
                Var cName=invoke.getInvokeExp().getArg(0);
                if(c!=null){
                    if(cName.isConst())
                    {
                        JClass ct=solver.getHierarchy().getClass(cName.getConstValue().toString().replaceAll("\"",""));
//                        if(ct!=null)
//                        System.out.println("***"+ct.getName());
                        class_pts.put(c,ct);
                        //System.out.println("***"+c.getName()+" "+ct.getName());
                    }
                }
            }
        }
    }
    @Override
    public void onProcessCall(Var var,Invoke invoke){
        if(invoke.isVirtual()){
        //System.out.println("********************"+invoke.getInvokeExp().getMethodRef().toString());
            String[] method=invoke.getMethodRef().toString().split("\\s+");
            if(method[1].equals("java.lang.reflect.Method")){
                String[] name=method[2].split("\\(");
                if(name[0].equals("getMethod")){//case 2: getMtd
                    Var m=invoke.getResult();
                    Var mName=invoke.getInvokeExp().getArg(0);
                    if(m!=null){
                        if(mName.isConst()){
                            JClass c_=class_pts.get(var);
                            if(c_!=null){
                                //JMethod mt=solver.getHierarchy().getMethod(mName.getConstValue().toString().replaceAll("\"",""));
                                JMethod mt=c_.getDeclaredMethod(mName.getConstValue().toString().replaceAll("\"",""));
                                if(mt!=null)
                                    System.out.println("***"+mt.getName());
                            }
                        }
                    }
                }
            }
        }
    }
}
