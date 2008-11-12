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

package edu.mit.csail.sdg.alloy4compiler.parser;

import java.util.LinkedList;
import java.util.List;
import java.io.Reader;
import java_cup_11a.runtime.Scanner;
import java_cup_11a.runtime.Symbol;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import static edu.mit.csail.sdg.alloy4compiler.parser.CompSym.*;

/**
 * This class sits between the lexer and the parser.
 *
 * <p>
 * Reason: there are 3 sets of "special tokens" that the lexer will not output.
 * But the Parser expects them. So this filter class observes the stream of
 * tokens, and intelligently merges or changes some primitive tokens into special tokens.
 *
 * <p>
 * For more details, refer to the main documentation.
 * But, very briefly, here are the 3 groups:
 *
 * <p>
 * (1) The lexer will generate only ALL, NO, LONE, ONE, SUM, SOME.
 * It will not output ALL2, NO2, LONE2, ONE2, SUM2, SOME2.
 * (The Filter class will change ONE into ONE2 when appropriate)
 *
 * <p>
 * (2) The lexer won't output NOTEQUALS, NOTIN, NOTLT, NOTLTE, NOTGT, NOTGTE.
 * Instead it outputs them as separate tokens (eg. "NOT" "EQUALS").
 * (The Filter class is used to merge them into a single "NOTEQUALS" token)
 *
 * <p>
 * (3) The lexer won't output the 15 special arrows (eg. ONE_ARROW_ONE)
 * Instead it outputs them as separate tokens (eg. "ONE", "ARROW", "ONE")
 * (The Filter class is used to merge them into a single "ONE_ARROW_ONE" token)
 */

final class CompFilter implements Scanner {

    //===================== PHASE 1 ==================================================================================

    /** The underlying lexer. */
    private final Scanner r;

    /** A list of tokens that we prefetched from the underlying lexer. */
    private final LinkedList<Symbol> undo = new LinkedList<Symbol>();

    /** Stores the latest token passed from phase 1 to phase 2. */
    private Symbol last = null;

    /** Reads a token from the underlying lexer; if the undo list is not empty, we take it from there instead. */
    private Symbol myread() throws Err {
      if (!undo.isEmpty()) return undo.removeFirst();
      try {
          return r.next_token();
      } catch(Exception ex) {
          throw new ErrorFatal("IO error: "+ex.getMessage(), ex);
      }
    }

    /** Reads one or more tokens from the underlying lexer, transform them, then give it to phase 2. */
    public Symbol next_token() throws Err {
       Symbol a = myread();
       if (a.sym==LONE) {
          Symbol b = myread();
          if (last==null || last.sym!=COLON) return last=decl(a,b,LONE2); else undo.add(0,b);
       }
       if (a.sym==ONE) {
          Symbol b = myread();
          if (last==null || last.sym!=COLON) return last=decl(a,b,ONE2); else undo.add(0,b);
       }
       if (a.sym==SOME) {
          Symbol b = myread();
          if (last==null || last.sym!=COLON) return last=decl(a,b,SOME2); else undo.add(0,b);
       }
       if (last==null || last.sym!=COLON) {
          if (a.sym==NO) return last=decl(a, myread(), NO2);
          if (a.sym==ALL) return last=decl(a, myread(), ALL2);
          if (a.sym==SUM) return last=decl(a, myread(), SUM2);
       }
       return last=a;
    }

    private Symbol decl(Symbol a,Symbol b,int c) throws Err {
        LinkedList<Symbol> temp=new LinkedList<Symbol>();
        if (b.sym==PRIVATE) {temp.add(b); b=myread();}
        if (b.sym==DISJ || b.sym==PART || b.sym==EXH) {temp.add(b); b=myread();}
        temp.add(b);
        if (b.sym==ID) {
            while(true) {
                temp.add(b=myread());
                if (b.sym==COLON) {a.sym=c; break;}
                if (b.sym!=COMMA) break;
                temp.add(b=myread());
                if (b.sym!=ID) break;
            }
        }
        for(int i=temp.size()-1; i>=0; i--) undo.add(0, temp.get(i));
        return a;
    }

   /** Change a.pos to be the merger of a.pos and b.pos, then change a.sym to be sym, then return a. */
   private static Symbol merge(Symbol a, Symbol b, int sym) {
      a.pos = a.pos.merge(b.pos);
      a.sym = sym;
      return a;
   }

   /** Construct a filter for the tokens from the given file. */
   public CompFilter(Module module, List<Object> seenDollar, String filename, int lineOffset, Reader i) throws Err {
      final CompLexer L = new CompLexer(i);
      L.alloy_module = module;
      L.alloy_filename = filename;
      L.alloy_lineoffset = lineOffset;
      L.alloy_seenDollar = seenDollar;
      // Replace "ID : RUN"   with "RUN . ID"
      // Replace "ID : CHECK" with "CHECK . ID"
      final Scanner A = new Scanner() {
         private Symbol a, b, c;
         public final Symbol next_token() throws Exception {
            if (a==null) a=L.next_token(); if (a.sym==EOF) { b=a; c=a; }
            if (b==null) b=L.next_token(); if (b.sym==EOF) { c=b; }
            if (c==null) c=L.next_token();
            if (a.sym==ID && b.sym==COLON && (c.sym==RUN || c.sym==CHECK)) { Symbol x=c; c=b; b=a; a=c; c=null; a.sym=DOT; return x; }
            Symbol x=a; a=b; b=c; c=null; return x;
         }
      };
      // Merges   "pred" "/" "xxx"       into the actual symbol
      // Merges   "fun" "/" "xxx"        into the actual symbol
      // Merges   ! { in = < <= > >= }   into a single symbol
      // Merges   {..}=>{..}             into a single symbol
      final Scanner B = new Scanner() {
         private Symbol undo;
         public final Symbol next_token() throws Exception {
            Symbol x = undo;
            undo = null;
            if (x==null) x = A.next_token();
            if (x.sym==NOT) {
               Symbol y = A.next_token();
               if (y.sym==IN)     return merge(x, y, NOTIN);
               if (y.sym==EQUALS) return merge(x, y, NOTEQUALS);
               if (y.sym==LT)     return merge(x, y, NOTLT);
               if (y.sym==LTE)    return merge(x, y, NOTLTE);
               if (y.sym==GT)     return merge(x, y, NOTGT);
               if (y.sym==GTE)    return merge(x, y, NOTGTE);
               undo = y;
            } else if (x.sym==PRED) {
               Symbol y = A.next_token();
               if (y.sym!=SLASH) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("totalOrder")) return merge(x, z, TOTALORDER);
               undo = z;
            } else if (x.sym==FUN) {
               Symbol y = A.next_token();
               if (y.sym!=SLASH) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("add"))   return merge(x, z, INTADD);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("sub"))   return merge(x, z, INTSUB);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("mul"))   return merge(x, z, INTMUL);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("div"))   return merge(x, z, INTDIV);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("rem"))   return merge(x, z, INTREM);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("min"))   return merge(x, z, INTMIN);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("max"))   return merge(x, z, INTMAX);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("next"))  return merge(x, z, INTNEXT);
               if (z.sym==ID && ((ExprVar)(z.value)).label.equals("String")) { z.pos=x.pos.merge(z.pos); z.value=ExprVar.make(z.pos, "fun/String"); return z; }
            } else if (x.sym==ONE) {
               Symbol y = A.next_token();
               if (y.sym!=ARROW) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ONE)  return merge(x, z, ONE_ARROW_ONE);
               if (z.sym==LONE) return merge(x, z, ONE_ARROW_LONE);
               if (z.sym==SOME) return merge(x, z, ONE_ARROW_SOME);
               if (z.sym==SET)  return merge(x, z, ONE_ARROW_ANY); else { undo=z; return merge(x, y, ONE_ARROW_ANY); }
            } else if (x.sym==LONE) {
               Symbol y = A.next_token();
               if (y.sym!=ARROW) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ONE)  return merge(x, z, LONE_ARROW_ONE);
               if (z.sym==LONE) return merge(x, z, LONE_ARROW_LONE);
               if (z.sym==SOME) return merge(x, z, LONE_ARROW_SOME);
               if (z.sym==SET)  return merge(x, z, LONE_ARROW_ANY); else { undo=z; return merge(x, y, LONE_ARROW_ANY); }
            } else if (x.sym==SOME) {
               Symbol y = A.next_token();
               if (y.sym!=ARROW) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ONE)  return merge(x, z, SOME_ARROW_ONE);
               if (z.sym==LONE) return merge(x, z, SOME_ARROW_LONE);
               if (z.sym==SOME) return merge(x, z, SOME_ARROW_SOME);
               if (z.sym==SET)  return merge(x, z, SOME_ARROW_ANY); else { undo=z; return merge(x, y, SOME_ARROW_ANY); }
            } else if (x.sym==SET) {
               Symbol y = A.next_token();
               if (y.sym!=ARROW) { undo=y; return x; }
               Symbol z = A.next_token();
               if (z.sym==ONE)  return merge(x, z, ANY_ARROW_ONE);
               if (z.sym==LONE) return merge(x, z, ANY_ARROW_LONE);
               if (z.sym==SOME) return merge(x, z, ANY_ARROW_SOME);
               if (z.sym==SET)  return merge(x, z, ARROW); else { undo=z; return merge(x, y, ARROW); }
            } else if (x.sym==ARROW) {
               Symbol z = A.next_token();
               if (z.sym==ONE)  return merge(x, z, ANY_ARROW_ONE);
               if (z.sym==LONE) return merge(x, z, ANY_ARROW_LONE);
               if (z.sym==SOME) return merge(x, z, ANY_ARROW_SOME);
               if (z.sym==SET)  return merge(x, z, ARROW); else { undo=z; }
            }
            return x;
         }
      };
      // Merge "- number" into "-number" whenever it is not immediately following ")" "]" "}" DISJ TOTALORDER INT SUM ID NUMBER STR IDEN THIS INTMIN INTMAX INTNEXT UNIV SIGINT NONE
      final Scanner C = new Scanner() {
         private Symbol last, undo;
         public final Symbol next_token() throws Exception {
            Symbol x = undo;
            undo = null;
            if (x==null) x = B.next_token();
            if (last!=null) {
               if (last.sym==RPAREN || last.sym==RBRACKET || last.sym==RBRACE  || last.sym==DISJ || last.sym==TOTALORDER || last.sym==INT)  return last = x;
               if (last.sym==SUM    || last.sym==ID       || last.sym==NUMBER  || last.sym==STR  || last.sym==IDEN       || last.sym==THIS) return last = x;
               if (last.sym==INTMIN || last.sym==INTMAX   || last.sym==INTNEXT || last.sym==UNIV || last.sym==SIGINT     || last.sym==NONE) return last = x;
            }
            if (x.sym==MINUS) {
               Symbol y = B.next_token();
               if (y.sym==NUMBER) {
                  ExprConstant num = (ExprConstant)(y.value);
                  y.pos   = x.pos.merge(y.pos);
                  y.value = ExprConstant.Op.NUMBER.make(y.pos, 0 - num.num);
                  return last = y;
               }
               undo = y;
               return last = x;
            }
            return last = x;
         }
      };
      this.r = C;
   }
}
