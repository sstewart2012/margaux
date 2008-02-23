/*
 * Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4whole;

import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options.SatSolver;
import edu.mit.csail.sdg.alloy4viz.StaticInstanceReader;

/**
 * This class is used by the Alloy developers to drive the regression test suite.
 * For a more detailed guide on how to use Alloy API, please see "ExampleUsingTheCompiler.java"
 */

public final class SimpleCLI {

    private static final class SimpleReporter extends A4Reporter {

        private final StringBuilder sb = new StringBuilder();

        private final List<ErrorWarning> warnings = new ArrayList<ErrorWarning>();

        @Override public void debug(String msg) { sb.append(msg); }

        @Override public void parse(String msg) { sb.append(msg); }

        @Override public void typecheck(String msg) { sb.append(msg); }

        @Override public void warning(ErrorWarning msg) { warnings.add(msg); }

        @Override public void scope(String msg) { sb.append("   "); sb.append(msg); }

        @Override public void bound(String msg) { sb.append("   "); sb.append(msg); }

        @Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
            sb.append("   Solver="+solver+" Bitwidth="+bitwidth+" MaxSeq="+maxseq+" Symmetry="+(symmetry>0 ? (""+symmetry) : "OFF")+"\n");
        }

        @Override public void solve(int primaryVars, int totalVars, int clauses) {
            if (db) db("   "+totalVars+" vars. "+primaryVars+" primary vars. "+clauses+" clauses.\n");
            sb.append("   "+totalVars+" vars. "+primaryVars+" primary vars. "+clauses+" clauses. 12345ms.\n");
        }

        @Override public void resultCNF(String filename) {}

        @Override public void resultSAT(Object command, long solvingTime, Object solution) {
            if (db) db("   SAT!\n");
            if (!(command instanceof Command)) return;
            Command cmd = (Command)command;
            sb.append(cmd.check ? "   Counterexample found. " : "   Instance found. ");
            if (cmd.check) sb.append("Assertion is invalid"); else sb.append("Predicate is consistent");
            if (cmd.expects==0) sb.append(", contrary to expectation"); else if (cmd.expects==1) sb.append(", as expected");
            sb.append(". "+solvingTime+"ms.\n\n");
        }

        @Override public void resultUNSAT(Object command, long solvingTime, Object solution) {
            if (db) db("   UNSAT!\n");
            if (!(command instanceof Command)) return;
            Command cmd = (Command)command;
            sb.append(cmd.check ? "   No counterexample found." : "   No instance found.");
            if (cmd.check) sb.append(" Assertion may be valid"); else sb.append(" Predicate may be inconsistent");
            if (cmd.expects==1) sb.append(", contrary to expectation"); else if (cmd.expects==0) sb.append(", as expected");
            sb.append(". "+solvingTime+"ms.\n\n");
        }
    }

    private static boolean db=true;

    private static void db(String msg) { System.out.print(msg); System.out.flush(); }

    private SimpleCLI() { }

    private static void validate(A4Solution sol) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        sol.writeXML(pw, null, null);
        pw.flush();
        sw.flush();
        String txt = sw.toString();
        A4SolutionReader.read(new ArrayList<Sig>(), new XMLNode(new StringReader(txt)));
        StaticInstanceReader.parseInstance(new StringReader(txt));
    }

    public static void main(String[] args) throws Exception {
        SatSolver solver = A4Options.SatSolver.make("mem", "mem", "/tmp/sat/mem");
        final SimpleReporter rep = new SimpleReporter();
        for(String filename:args) {
            try {
                rep.sb.append("\n\nMain file = "+filename+"\n");
                if (db) db("Parsing+Typechecking...");
                Module world = CompUtil.parseEverything_fromFile(rep, null, filename);
                if (db) db(" ok\n");
                List<Pair<Command,Expr>> cmds=world.getAllCommandsWithFormulas();
                for(ErrorWarning msg: rep.warnings) rep.sb.append("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
                rep.warnings.clear();
                A4Options options = new A4Options();
                options.originalFilename = filename;
                options.solverDirectory = "/zweb/zweb/tmp/alloy4/x86-freebsd";
                options.solver = solver;
                //options.solver = A4Options.SatSolver.MiniSatJNI;
                if (args.length!=1) continue;
                for (int i=0; i<cmds.size(); i++) {
                    Command c = cmds.get(i).a;
                    if (db) {
                        String cc = c.toString();
                        if (cc.length()>60) cc=cc.substring(0,55);
                        db("Executing "+cc+"...\n");
                    }
                    rep.sb.append("Executing \""+c+"\"\n");
                    Expr facts = ExprConstant.TRUE;
                    for(Module m:world.getAllReachableModules()) for(Pair<String,Expr> f:m.getAllFacts()) facts=facts.and(f.b);
                    options.skolemDepth=0;
                    A4Solution s = TranslateAlloyToKodkod.execute_commandFromBook(rep, world.getAllReachableSigs(), facts.and(cmds.get(i).b), c, options);
                    if (s.satisfiable()) { validate(s); if (s.isIncremental()) { s=s.next(); if (s.satisfiable()) validate(s); } }
                    options.skolemDepth=2;
                    s = TranslateAlloyToKodkod.execute_commandFromBook(rep, world.getAllReachableSigs(), facts.and(cmds.get(i).b), c, options);
                    if (s.satisfiable()) { validate(s); if (s.isIncremental()) { s=s.next(); if (s.satisfiable()) validate(s); } }
                }
            } catch(Throwable ex) {
                rep.sb.append("\n\nException: "+ex);
            }
            if (db) { if (args.length!=1) db(" ERROR!\n"); else db("\n\n"); }
        }
        RandomAccessFile file=new RandomAccessFile(".alloy.tmp","rw");
        file.setLength(0);
        file.write(rep.sb.toString().getBytes("UTF-8"));
        file.close();
    }
}
