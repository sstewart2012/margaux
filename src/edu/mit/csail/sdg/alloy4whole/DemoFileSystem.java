/*
 * Alloy Analyzer
 * Copyright (c) 2007 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */

package edu.mit.csail.sdg.alloy4whole;

import static edu.mit.csail.sdg.alloy4.A4Reporter.NOP;
import java.util.LinkedHashSet;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

/** This class demonstrates how to access Alloy4 via the API, by modeling a simple file system. */

public class DemoFileSystem {

    /* Helper methods to simplify using the API for this example. */

    Set<Sig> sigs = new LinkedHashSet<Sig>();

    PrimSig makeSig(String name, boolean isAbstract, boolean isOne) throws Err {
        PrimSig ans = new PrimSig(null, null, name, isAbstract, false, isOne, false, false);
        sigs.add(ans);
        return ans;
    }

    PrimSig makeSig(PrimSig parent, String name, boolean isAbstract, boolean isOne) throws Err {
        PrimSig ans = new PrimSig(null, parent, name, isAbstract, false, isOne, false, false);
        sigs.add(ans);
        return ans;
    }

    void runFor3(Expr expr) throws Err {
        A4Options opt = new A4Options();
        Command cmd = new Command(null, "", false, 3, 3, 3, -1, null);
        A4Solution sol = TranslateAlloyToKodkod.execute_command(NOP, sigs, expr.and(fact), cmd, opt);
        System.out.println(sol.toString().trim());
        if (sol.satisfiable()) {
            System.out.println("In particular, File = " + sol.eval(file));
            System.out.println("In particular, Dir = " + sol.eval(dir));
            System.out.println("In particular, contains = " + sol.eval(contains));
            System.out.println("In particular, parent = " + sol.eval(parent));
        }
        System.out.println();
    }

    /* These corresponds to the helper predicates/functions provided in util/*.als */

    static Expr acyclic(Expr r) throws Err {
        ExprVar x = r.join(Sig.UNIV).oneOf("x");     // x is a variable over the domain of r
        return x.in(x.join(r.closure())).not().forAll(x); // (x !in x.^r) for all x
    }

    /* Here are definitions common to all instances. */

    PrimSig obj, dir, file, root;

    Field parent, contains;

    Expr fact = ExprConstant.TRUE;

    void makeDomain() throws Err {
        // abstract sig Obj { parent: lone Dir }
        // abstract sig Dir extends Obj { contains: set Obj }
        // abstract sig File extends Obj { }
        // one sig Root extends Dir { }
        obj  = makeSig("Obj", true, false);
        dir  = makeSig(obj, "Dir", true, false);
        file = makeSig(obj, "File", true, false);
        root = makeSig(dir, "Root", false, true);
        parent = obj.addField(null, "parent", dir.loneOf());
        contains = dir.addField(null, "contains", obj.setOf());
        // fact { all x:Obj-Root | one x.parent }
        ExprVar x = obj.minus(root).oneOf("x");
        fact = x.join(parent).one().forAll(x).and(fact);
        // fact { contains = ~ parent }
        fact = contains.equal(parent.transpose()).and(fact);
        // fact { acyclic[contains] }
        fact = acyclic(contains).and(fact);
    }

    /* Here is instance number 1. */

    void makeInstance1() throws Err {
        // file = F1, F2, F3
        // dir = Root, D1, D2
        // F1.parent = D1
        // F2.parent = D2
        // F3.parent = D2
        // D2.parent = D1
        // D1.parent = Root
        PrimSig file1 = makeSig(file, "F1", false, true);
        PrimSig file2 = makeSig(file, "F2", false, true);
        PrimSig file3 = makeSig(file, "F3", false, true);
        PrimSig dir1 = makeSig(dir, "D1", false, true);
        PrimSig dir2 = makeSig(dir, "D2", false, true);
        fact = file1.join(parent).equal(dir1).and(fact);
        fact = file2.join(parent).equal(dir2).and(fact);
        fact = file3.join(parent).equal(dir2).and(fact);
        fact = dir2.join(parent).equal(dir1).and(fact);
        fact = dir1.join(parent).equal(root).and(fact);
    }

    /* Here is instance number 2. */

    void makeInstance2() throws Err {
        // dir = Root, D1, D2
        // D2.parent = D1
        // D1.parent = D2
        PrimSig dir1 = makeSig(dir, "D1", false, true);
        PrimSig dir2 = makeSig(dir, "D2", false, true);
        fact = dir2.join(parent).equal(dir1).and(fact);
        fact = dir1.join(parent).equal(dir2).and(fact);
    }

    private DemoFileSystem() { }

    /* The main driver. */

    public static void main(String[] args) throws Err {

        DemoFileSystem x;

        x = new DemoFileSystem();
        x.makeDomain();
        x.makeInstance1();
        x.runFor3(x.file.some()); // run { some file }

        x = new DemoFileSystem();
        x.makeDomain();
        x.makeInstance1();
        x.runFor3(acyclic(x.contains).not()); // run { !acyclic[contains] }
    }
}