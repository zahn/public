/* Generated By:JJTree: Do not edit this line. ASTCompound.java */

package org.cs3.pl.parser.internal.classic;

public class ASTCompound extends SimpleNode implements ASTTerm{
  private ASTSequence args;

public ASTCompound(int id) {
    super(id);
  }

  public String getName() {
  	if(children[0] instanceof ASTFunctor)
  		return ((ASTFunctor)children[0]).getName();
  	if(children[0] instanceof ASTAtom)
  		return ((ASTAtom)children[0]).getName();
  	if(children[0] instanceof ASTVariable)
  		return ((ASTVariable)children[0]).getName();
  	if(children[0] instanceof ASTBinaryOp)
  		return ((ASTBinaryOp)children[0]).getStartToken().toString();
  	throw new IllegalStateException("The first child of a Compound must be an atom or a variable.");
  }
  
  public ASTCompound(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

public void setArgs(ASTSequence args) {
	this.args = args;

	
}

public ASTSequence getArgs() {
	return args;
}
}
