/* Generated By:JJTree: Do not edit this line. ASTPredicateHead.java */

package org.cs3.pl.parser.internal.classic;

public class ASTPredicateHead extends SimpleNode {
	public ASTPredicateHead(int id) {
		super(id);
	}
	
	public ASTPredicateHead(PrologParser p, int id) {
		super(p, id);
	}
	
	
	/** Accept the visitor. **/
	public Object jjtAccept(PrologParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
	
	/**
	 * 
	 */
	public String getName() {
		return ((ASTFunctor)jjtGetChild(0)).getName();
	}
}
