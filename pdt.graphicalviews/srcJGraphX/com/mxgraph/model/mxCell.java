/**
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mxgraph.io.graphml.mxGraphMlData;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;

/**
 * Cells are the elements of the graph model. They represent the state of the
 * groups, vertices and edges in a graph.
 *
 * <h4>Edge Labels</h4>
 * 
 * Using the x- and y-coordinates of a cell's geometry it is possible to
 * position the label on edges on a specific location on the actual edge shape
 * as it appears on the screen. The x-coordinate of an edge's geometry is used
 * to describe the distance from the center of the edge from -1 to 1 with 0
 * being the center of the edge and the default value. The y-coordinate of an
 * edge's geometry is used to describe the absolute, orthogonal distance in
 * pixels from that point. In addition, the mxGeometry.offset is used as a
 * absolute offset vector from the resulting point.
 * 
 * The width and height of an edge geometry are ignored.
 * 
 * To add more than one edge label, add a child vertex with a relative geometry.
 * The x- and y-coordinates of that geometry will have the same semantiv as the
 * above for edge labels.
 */
public class mxCell implements mxICell, Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 910211337632342672L;

	/**
	 * Holds the Id. Default is null.
	 */
	protected String id;
	
	/**
	 * Holds the Id generated by Pdt. Default is null.
	 */
	protected String pdtId;

	/**
	 * Holds the user object. Default is null.
	 */
	protected Object value;

	/**
	 * Holds the geometry. Default is null.
	 */
	protected mxGeometry geometry;

	/**
	 * Holds the style as a string of the form stylename[;key=value]. Default is
	 * null.
	 */
	protected String style;

	/**
	 * Specifies whether the cell is a vertex or edge and whether it is
	 * connectable, visible and collapsed. Default values are false, false,
	 * true, true and false respectively.
	 */
	protected boolean vertex = false, edge = false, connectable = true, visible = true, collapsed = false;

	/**
	 * Reference to the parent cell and source and target terminals for edges.
	 */
	protected mxICell parent, source, target;

	/**
	 * Holds the child cells and connected edges.
	 */
	protected List<Object> children, edges;

	private List<Object> incomingEdges;

	private List<Object> outgoingEdges;

	private String toolTip;

	/**
	 * Constructs a new cell with an empty user object.
	 */
	public mxCell() {
		this(null);
	}

	/**
	 * Constructs a new cell for the given user object.
	 * 
	 * @param value
	 *            Object that represents the value of the cell.
	 */
	public mxCell(Object value) {
		this(value, null, null);
	}

	/**
	 * Constructs a new cell for the given parameters.
	 * 
	 * @param value
	 *            Object that represents the value of the cell.
	 * @param geometry
	 *            Specifies the geometry of the cell.
	 * @param style
	 *            Specifies the style as a formatted string.
	 */
	public mxCell(Object value, mxGeometry geometry, String style) {
		setValue(value);
		setGeometry(geometry);
		setStyle(style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getId()
	 */
	public String getId() {
		return id;
	}

	public String getPdtId() {
		return pdtId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setId(String)
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public void setPdtId(String pdtId) {
		this.pdtId = pdtId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setValue(Object)
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getGeometry()
	 */
	public mxGeometry getGeometry() {
		return geometry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setGeometry(com.mxgraph.model.mxGeometry)
	 */
	public void setGeometry(mxGeometry geometry) {
		this.geometry = geometry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getStyle()
	 */
	public String getStyle() {
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setStyle(String)
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#isVertex()
	 */
	public boolean isVertex() {
		return vertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setVertex(boolean)
	 */
	public void setVertex(boolean vertex) {
		this.vertex = vertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#isEdge()
	 */
	public boolean isEdge() {
		return edge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setEdge(boolean)
	 */
	public void setEdge(boolean edge) {
		this.edge = edge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#isConnectable()
	 */
	public boolean isConnectable() {
		return connectable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setConnectable(boolean)
	 */
	public void setConnectable(boolean connectable) {
		this.connectable = connectable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#isVisible()
	 */
	public boolean isVisible() {
		return visible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#isCollapsed()
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setCollapsed(boolean)
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getParent()
	 */
	public mxICell getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setParent(com.mxgraph.model.mxICell)
	 */
	public void setParent(mxICell parent) {
		this.parent = parent;
	}

	/**
	 * Returns the source terminal.
	 */
	public mxICell getSource() {
		return source;
	}

	/**
	 * Sets the source terminal.
	 * 
	 * @param source
	 *            Cell that represents the new source terminal.
	 */
	public void setSource(mxICell source) {
		this.source = source;
	}

	/**
	 * Returns the target terminal.
	 */
	public mxICell getTarget() {
		return target;
	}

	/**
	 * Sets the target terminal.
	 * 
	 * @param target
	 *            Cell that represents the new target terminal.
	 */
	public void setTarget(mxICell target) {
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getTerminal(boolean)
	 */
	public mxICell getTerminal(boolean source) {
		return (source) ? getSource() : getTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#setTerminal(com.mxgraph.model.mxICell,
	 * boolean)
	 */
	public mxICell setTerminal(mxICell terminal, boolean isSource) {
		if (isSource) {
			setSource(terminal);
		} else {
			setTarget(terminal);
		}

		return terminal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getChildCount()
	 */
	public int getChildCount() {
		return (children != null) ? children.size() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getIndex(com.mxgraph.model.mxICell)
	 */
	public int getIndex(mxICell child) {
		return (children != null) ? children.indexOf(child) : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getChildAt(int)
	 */
	public mxICell getChildAt(int index) {
		if (children != null) {
			if (children.size() > index) {
				return (mxICell) children.get(index);
			}
		} 
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#insert(com.mxgraph.model.mxICell)
	 */
	public mxICell insert(mxICell child) {
		int index = getChildCount();

		if (child.getParent() == this) {
			index--;
		}

		return insert(child, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#insert(com.mxgraph.model.mxICell, int)
	 */
	public mxICell insert(mxICell child, int index) {
		if (child != null) {
			child.removeFromParent();
			child.setParent(this);

			if (children == null) {
				children = new ArrayList<Object>();
				children.add(child);
			} else {
				// NEW compute index TODO do not overwrite a parameter!
				/*
				 * double x = child.getGeometry().getCenterX(); for (index = 0;
				 * index < children.size(); index++) { mxICell oldChild =
				 * (mxICell) children.get(index); if
				 * (oldChild.getGeometry().getCenterX() > x) { break; } }
				 */ // problem: moving an edge might break the order
				children.add(index, child);
			}
		}

		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#remove(int)
	 */
	public mxICell remove(int index) {
		mxICell child = null;

		if (children != null && index >= 0) {
			child = getChildAt(index);
			remove(child);
		}

		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#remove(com.mxgraph.model.mxICell)
	 */
	public mxICell remove(mxICell child) {
		if (child != null && children != null) {
			children.remove(child);
			child.setParent(null);
		}

		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#removeFromParent()
	 */
	public void removeFromParent() {
		if (parent != null) {
			parent.remove(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getEdgeCount()
	 */
	public int getEdgeCount() {
		return (edges != null) ? edges.size() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getEdgeIndex(com.mxgraph.model.mxICell)
	 */
	public int getEdgeIndex(mxICell edge) {
		return (edges != null) ? edges.indexOf(edge) : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getEdgeAt(int)
	 */
	public mxICell getEdgeAt(int index) {
		return (edges != null) ? (mxICell) edges.get(index) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#insertEdge(com.mxgraph.model.mxICell,
	 * boolean)
	 */
	public mxICell insertEdge(mxICell edge, boolean isOutgoing) {
		if (edge != null) {
			edge.removeFromTerminal(isOutgoing);
			edge.setTerminal(this, isOutgoing);

			if (edges == null || edge.getTerminal(!isOutgoing) != this || !edges.contains(edge)) {
				if (edges == null) {
					edges = new ArrayList<Object>();
				}

				edges.add(edge);
			}
		}

		return edge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#removeEdge(com.mxgraph.model.mxICell,
	 * boolean)
	 */
	public mxICell removeEdge(mxICell edge, boolean isOutgoing) {
		if (edge != null) {
			if (edge.getTerminal(!isOutgoing) != this && edges != null) {
				edges.remove(edge);
			}

			edge.setTerminal(null, isOutgoing);
		}

		return edge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#removeFromTerminal(boolean)
	 */
	public void removeFromTerminal(boolean isSource) {
		mxICell terminal = getTerminal(isSource);

		if (terminal != null) {
			terminal.removeEdge(this, isSource);
		}
	}

	/**
	 * Returns the specified attribute from the user object if it is an XML
	 * node.
	 * 
	 * @param name
	 *            Name of the attribute whose value should be returned.
	 * @return Returns the value of the given attribute or null.
	 */
	public String getAttribute(String name) {
		return getAttribute(name, null);
	}

	/**
	 * Returns the specified attribute from the user object if it is an XML
	 * node.
	 * 
	 * @param name
	 *            Name of the attribute whose value should be returned.
	 * @param defaultValue
	 *            Default value to use if the attribute has no value.
	 * @return Returns the value of the given attribute or defaultValue.
	 */
	public String getAttribute(String name, String defaultValue) {
		Object userObject = getValue();
		String val = null;

		if (userObject instanceof Element) {
			Element element = (Element) userObject;
			val = element.getAttribute(name);
		}

		if (val == null) {
			val = defaultValue;
		}

		return val;
	}

	/**
	 * Sets the specified attribute on the user object if it is an XML node.
	 * 
	 * @param name
	 *            Name of the attribute whose value should be set.
	 * @param value
	 *            New value of the attribute.
	 */
	public void setAttribute(String name, String value) {
		Object userObject = getValue();

		if (userObject instanceof Element) {
			Element element = (Element) userObject;
			element.setAttribute(name, value);
		}
	}

	/**
	 * Returns a clone of the cell.
	 */
	public Object clone() throws CloneNotSupportedException {
		mxCell clone = (mxCell) super.clone();

		clone.setValue(cloneValue());
		clone.setStyle(getStyle());
		clone.setCollapsed(isCollapsed());
		clone.setConnectable(isConnectable());
		clone.setEdge(isEdge());
		clone.setVertex(isVertex());
		clone.setVisible(isVisible());
		clone.setParent(null);
		clone.setSource(null);
		clone.setTarget(null);
		clone.children = null;
		clone.edges = null;

		mxGeometry geometry = getGeometry();

		if (geometry != null) {
			clone.setGeometry((mxGeometry) geometry.clone());
		}

		return clone;
	}

	/**
	 * Returns a clone of the user object. This implementation clones any XML
	 * nodes or otherwise returns the same user object instance.
	 */
	protected Object cloneValue() {
		Object value = getValue();

		if (value instanceof Node) {
			value = ((Node) value).cloneNode(true);
		}

		return value;
	}

	/*public void setDataMap(HashMap<String, mxGraphMlData> dataMap) {
		this.dataMap = dataMap;
	}*/
	
	@Override
	public void sortEdges() { //called after sort
		incomingEdges = new ArrayList<Object>(); 
		outgoingEdges = new ArrayList<Object>();
		for (int i=0; i<edges.size(); i++) {
			mxCell iEdge = (mxCell) edges.get(i);
			if (iEdge.getSource() == iEdge.getTarget()) {
				continue; //recursive edges always use port x = 0
			}
			if (iEdge.getSource() == this) {
				incomingEdges.add(iEdge);
			} else {
				outgoingEdges.add(iEdge);
			}
		}
		sortEdges(incomingEdges);
		sortEdges(outgoingEdges);
	}

	private void sortEdges(List<Object> edges) { 
		if (edges == null) {
			return;
		}
		Collections.sort(edges, new Comparator<Object>() { // this can be simplified with Java 8
			public int compare(Object o1, Object o2) {
				mxCell edge1 = (mxCell) o1;
				mxCell edge2 = (mxCell) o2;
				mxCell cell1source = (mxCell) edge1.getSource();
				mxCell cell2source = (mxCell) edge2.getSource();
				mxCell cell1target = (mxCell) edge1.getTarget();
				mxCell cell2target = (mxCell) edge2.getTarget();
				double x1source = cell1source.getAbsX();
				double x1target = cell1target.getAbsX();
				double x2source = cell2source.getAbsX();
				double x2target = cell2target.getAbsX();
				Double x1 = x1source + x1target; //- g1.getOffset().getX(); 
				Double x2 = x2source + x2target; //- g2.getOffset().getX();
				//System.out.println(cell1target.value + "x:" + x1 + cell2target.value + "x:" + x2);
				return x1.compareTo(x2);
			}
		});
	}

	public double getAbsX() { 
		double x = getGeometry().getX(); //10 //unify: 166/246 //abc: 126
		if (parent.getGeometry() != null) {
			x+= parent.getGeometry().getX(); //396 //meta: 756  
		}
		return x; //406 //unify: 562/1002 //abc: 522
	}
	
	public double getAbsY() { 
		double y = getGeometry().getY(); 
		if (parent.getGeometry() != null) {
			y += parent.getGeometry().getY();   
		}
		return y;
	}

	@Override
	public int getEdgeCount(boolean isSource) {
		if (isSource) {
			return incomingEdges.size();
		} else {
			return outgoingEdges.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mxgraph.model.mxICell#getEdgeIndex(com.mxgraph.model.mxICell)
	 */
	public int getEdgeIndexSeparated(mxICell edge, boolean isSource) {
		if (isSource) {
			return incomingEdges.indexOf(edge);
		}
		return outgoingEdges.indexOf(edge);
	}

	/**
	 * sorts edges and then divides the index of edge through all
	 * incoming/outgoing edges
	 * 
	 * @param edge
	 *            that is connected to this cell
	 * @param isSource
	 *            describes whether this cell is its source or target
	 * @return 0 < x < 1 describes a point of the cell's border
	 */
	public double computePort(mxICell edge, boolean isSource) {
		if (edge.getTerminal(true) == edge.getTerminal(false)) {
			return 1; //recursive call
		}
		sortEdges();

		// compute x coordinates by connected vertices' x
		int indexEntry = getEdgeIndexSeparated(edge, isSource) + 1;
		// the port should not be 0

		int nSource = getEdgeCount(isSource) + 1;
		// the port should not be 1

		double exitX = (double) indexEntry / nSource;
		return exitX;
	}

	public String getToolTip() {
		//System.out.println("getToolTip:" + toolTip);
		return this.toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
		//System.out.println("setToolTip:" + toolTip);
	}

	public mxPoint getTerminalPoint(boolean isSource) {
		mxCell terminalCell = (mxCell) getTerminal(isSource);
		double port = terminalCell.computePort(this, isSource);
		double width = terminalCell.getGeometry().getWidth();
		double x = terminalCell.getAbsX() + port * width;
		double y = terminalCell.getAbsY();
		return new mxPoint(x, y);
	}
}