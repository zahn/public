/* Generated By:JJTree: Do not edit this line. ASTAtom.java */

package org.cs3.pl.parser.internal.classic;

public class ASTAtom extends ASTNamedNode {
  public ASTAtom(int id) {
    super(id);
  }

  public ASTAtom(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
