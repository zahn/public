/* Generated By:JJTree: Do not edit this line. ASTInitialization.java */

package org.cs3.pl.parser.internal.classic;

public class ASTInitialization extends SimpleNode {
  public ASTInitialization(int id) {
    super(id);
  }

  public ASTInitialization(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
