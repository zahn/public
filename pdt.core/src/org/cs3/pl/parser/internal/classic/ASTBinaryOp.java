/* Generated By:JJTree: Do not edit this line. ASTBinaryOp.java */

package org.cs3.pl.parser.internal.classic;

public class ASTBinaryOp extends SimpleNode {
  public ASTBinaryOp(int id) {
    super(id);
  }

  public ASTBinaryOp(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }


}
