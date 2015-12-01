/**
 * Copyright (c) 2010 David Benson, Gaudenz Alder
 */
package com.mxgraph.io.graphml;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxConnectionConstraint;
import com.mxgraph.view.mxGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a Graph element in the GML Structure.
 */
public class mxGraphMlGraph {
	/**
	 * Map with the vertex cells added in the addNode method.
	 */
	private static HashMap<String, Object> cellsMap = new HashMap<String, Object>();

	private String id = "";

	private String edgedefault = "";

	private List<mxGraphMlNode> nodes = new ArrayList<mxGraphMlNode>();

	private List<mxGraphMlEdge> edges = new ArrayList<mxGraphMlEdge>();

	/**
	 * Constructs a graph with id and edge default direction.
	 * 
	 * @param id
	 *            Graph's ID
	 * @param edgedefault
	 *            Edge Default direction.("directed" or "undirected")
	 */
	public mxGraphMlGraph(String id, String edgedefault) {
		this.id = id;
		this.edgedefault = edgedefault;
	}

	/**
	 * Constructs an empty graph.
	 */
	public mxGraphMlGraph() {
	}

	/**
	 * Constructs a graph from a xml graph element.
	 * 
	 * @param graphElement
	 *            Xml graph element.
	 */
	public mxGraphMlGraph(Element graphElement) {
		this.id = graphElement.getAttribute(mxGraphMlConstants.ID);
		this.edgedefault = graphElement.getAttribute(mxGraphMlConstants.EDGE_DEFAULT);

		// Adds node elements
		List<Element> nodeElements = mxGraphMlUtils.childsTags(graphElement, mxGraphMlConstants.NODE);

		for (Element nodeElem : nodeElements) {
			mxGraphMlNode node = new mxGraphMlNode(nodeElem);

			nodes.add(node);
		}

		// Adds edge elements
		List<Element> edgeElements = mxGraphMlUtils.childsTags(graphElement, mxGraphMlConstants.EDGE);

		for (Element edgeElem : edgeElements) {
			mxGraphMlEdge edge = new mxGraphMlEdge(edgeElem);

			if (edge.getEdgeDirected().equals("")) {
				if (edgedefault.equals(mxGraphMlConstants.EDGE_DIRECTED)) {
					edge.setEdgeDirected("true");
				} else if (edgedefault.equals(mxGraphMlConstants.EDGE_UNDIRECTED)) {
					edge.setEdgeDirected("false");
				}
			}

			edges.add(edge);
		}
	}

	/**
	 * Adds the elements represented for this graph model into the given graph.
	 * 
	 * @param graph
	 *            Graph where the elements will be located
	 * @param parent
	 *            Parent of the cells to be added.
	 */
	public void addGraph(mxGraph graph, Object parent) {
		List<mxGraphMlNode> nodeList = getNodes();
		/*
		 * nodeList ArrayList<E> (id=207) . elementData Object[10] (id=209) .
		 * [0] mxGraphMlNode (id=211) . nodeDataMap HashMap<K,V> (id=216) .
		 * table HashMap$Node<K,V>[16] (id=223) . [13] HashMap$Node<K,V>
		 * (id=229) . value mxGraphMlData (id=232) . dataValue
		 * "Application Node 1" (id=236)
		 */

		for (mxGraphMlNode node : nodeList)
		/*
		 * mxGraphMlNode (id=211) . nodeDataMap HashMap<K,V> (id=216) . table
		 * HashMap$Node<K,V>[16] (id=223) . [13] HashMap$Node<K,V> (id=229) .
		 * value mxGraphMlData (id=232) . dataValue "Application Node 1"
		 * (id=236)
		 */
		{
			addNode(graph, parent, node);
		}
		List<mxGraphMlEdge> edgeList = getEdges();

		for (mxGraphMlEdge edge : edgeList) {
			addEdge(graph, parent, edge);
		}
	}

	/**
	 * Checks if the node has data elements inside.
	 * 
	 * @param node
	 *            Gml node element.
	 * @return Returns <code>true</code> if the node has data elements inside.
	 */
	public static boolean hasData(mxGraphMlNode node) {
		boolean ret = false;
		if (node.getNodeDataMap() == null) {
			ret = false;
		} else {
			ret = true;
		}
		return ret;
	}

	/**
	 * Returns the data element inside the node that references to the key
	 * element with name = KEY_NODE_NAME.
	 * 
	 * @param node
	 *            Gml Node element.
	 * @return The required data. null if not found.
	 */
	public static mxGraphMlData dataNodeKey(mxGraphMlNode node)
	/*
	 * pdt: node mxGraphMlNode (id=55) nodeDataMap HashMap<K,V> (id=62) table
	 * HashMap$Node <K,V>[16] (id=76) || keySet null [13] HashMap$Node <K,V>
	 * (id=79) key "value" (id=1497) || value mxGraphMlData (id=82) dataValue
	 * "Application Node 1" (id=84)
	 */
	{
		// This node is rather a whole graph... is the nesting too deep?

		String keyId = "";
		HashMap<String, mxGraphMlKey> keyMap = mxGraphMlKeyManager.getInstance().getKeyMap(); // hello:
																								// empty
		// Do all graphml data need a key definition? What about predefined
		// keys?

		for (mxGraphMlKey key : keyMap.values())
		/*
		 * pdt: node mxGraphMlKey (id=129) keyType mxGraphMlKey$keyTypeValues
		 * (id=134) name "BOOLEAN" (id=135)
		 */
		{
			if (key.getKeyName().equals(mxGraphMlConstants.KEY_NODE_NAME)) {
				keyId = key.getKeyId();
			}
		}

		mxGraphMlData data = null;
		HashMap<String, mxGraphMlData> nodeDataMap = node.getNodeDataMap(); // empty
		data = nodeDataMap.get(keyId); // null

		return data;
	}

	/**
	 * Returns the data element inside the edge that references to the key
	 * element with name = KEY_EDGE_NAME.
	 * 
	 * @param edge
	 *            Gml Edge element.
	 * @return The required data. null if not found.
	 */
	public static mxGraphMlData dataEdgeKey(mxGraphMlEdge edge) {
		String keyId = "";
		HashMap<String, mxGraphMlKey> keyMap = mxGraphMlKeyManager.getInstance().getKeyMap();
		for (mxGraphMlKey key : keyMap.values()) {
			if (key.getKeyName().equals(mxGraphMlConstants.KEY_EDGE_NAME)) {
				keyId = key.getKeyId();
			}
		}

		mxGraphMlData data = null;
		HashMap<String, mxGraphMlData> nodeDataMap = edge.getEdgeDataMap();
		data = nodeDataMap.get(keyId);

		return data;
	}

	/**
	 * Adds the vertex represented for the gml node into the graph with the
	 * given parent.
	 * 
	 * @param graph
	 *            Graph where the vertex will be added.
	 * @param parent
	 *            Parent's cell.
	 * @param node
	 *            Gml Node
	 * @return The inserted Vertex cell.
	 */
	private mxCell addNode(mxGraph graph, Object parent, mxGraphMlNode node) {
		/*
		 * mxGraphMlNode (id=211) . nodeDataMap HashMap<K,V> (id=216) . table
		 * HashMap$Node<K,V>[16] (id=223) . [13] HashMap$Node<K,V> (id=229) .
		 * value mxGraphMlData (id=232) . dataValue "Application Node 1"
		 * (id=236)
		 */

		mxCell v1;
		String id = node.getNodeId();

		mxGraphMlData data = dataNodeKey(node); // null

		if (data != null && data.getDataShapeNode() != null) {
			Double x = Double.valueOf(data.getDataShapeNode().getDataX());
			Double y = Double.valueOf(data.getDataShapeNode().getDataY());
			Double h = Double.valueOf(data.getDataShapeNode().getDataHeight());
			Double w = Double.valueOf(data.getDataShapeNode().getDataWidth());
			String label = data.getDataShapeNode().getDataLabel();
			String style = data.getDataShapeNode().getDataStyle();
			v1 = (mxCell) graph.insertVertex(parent, id, label, x, y, w, h, style);
		} else {
			HashMap<String, mxGraphMlData> dataMap = node.getNodeDataMap();
			String label = getLabel(dataMap);
			String style = // mxConstants.STYLE_STARTSIZE + "=10;" + no effect
			// mxConstants.STYLE_SWIMLANE_FILLCOLOR + "=orange;" + no effect
			mxConstants.STYLE_OVERFLOW + "=hidden;" + mxConstants.STYLE_SPACING + "=5;" + mxConstants.STYLE_FOLDABLE
					+ "=0;" + mxConstants.STYLE_FONTCOLOR + "=black;" + mxConstants.STYLE_LABEL_BACKGROUNDCOLOR + "="
					+ getHeaderColor(dataMap) + ";" + mxConstants.STYLE_VERTICAL_ALIGN + "=" + mxConstants.ALIGN_TOP
					+ ";" + mxConstants.STYLE_STROKECOLOR + "=" + getStrokeColor(dataMap) + ";"
					+ mxConstants.STYLE_DASHED + "=" + isDashed(dataMap) + ";" + mxConstants.STYLE_FILLCOLOR + "="
					+ getFillColor(dataMap);
			v1 = (mxCell) graph.insertVertex(parent, id, label, 0, 0, 100, 100, style);
			/*
			 * for (String key : dataMap.keySet()) { v1.setAttribute(key,
			 * dataMap.get(key).getDataValue()); } String test =
			 * v1.getAttribute("kind"); if
			 * (getStrokeColor(dataMap).equals("red")) {
			 * System.out.println("test:" + test); }
			 */// instead:
			v1.setDataMap(dataMap);
		}
		cellsMap.put(id, v1);
		List<mxGraphMlGraph> graphs = node.getNodeGraph();

		for (mxGraphMlGraph gmlGraph : graphs) {
			gmlGraph.addGraph(graph, v1);
		}
		return v1;
	}

	private String getHeaderColor(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("file")) {
			mxGraphMlData moduleEntry = dataMap.get("module");
			if (moduleEntry != null) {
				String moduleValue = moduleEntry.getDataValue();
				if (!moduleValue.equals("user")) {
					return "grey";
				} else {
					return "white";
				}
			}
		}
		return "none";
	}

	private String getStrokeColor(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("predicate")) {
			mxGraphMlData isUnusedLocalEntry = dataMap.get("isUnusedLocal");
			if (isUnusedLocalEntry != null) {
				String isUnusedLocalValue = isUnusedLocalEntry.getDataValue();
				if (isUnusedLocalValue.equals("true")) {
					return "red";
				}
			}
		}
		return "black";
	}

	private String isDashed(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("predicate")) {
			mxGraphMlData isDynamicEntry = dataMap.get("isDynamic");
			if (isDynamicEntry != null) {
				String isDynamicValue = isDynamicEntry.getDataValue();
				if (isDynamicValue.equals("true")) {
					return "1";
				}
			}
		}
		return "0";
	}

	private String getFillColor(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (!kindValue.equals("predicate")) {
			return "white";
		}
		mxGraphMlData isExportedEntry = dataMap.get("isExported");
		if (isExportedEntry != null) {
			String isExportedValue = isExportedEntry.getDataValue();
			if (isExportedValue.equals("true")) {
				return "green";
			}
		}
		return "yellow";
	}

	private String getLabel(HashMap<String, mxGraphMlData> dataMap) {
		String label = "";
		mxGraphMlData dataEntry = dataMap.get("value");
		if (dataEntry == null) {
			dataEntry = dataMap.get("label");
		}
		if (dataEntry == null) {
			dataEntry = dataMap.get("functor");
		}
		if (dataEntry != null) {
			label = dataEntry.getDataValue();
		}
		return label;
	}

	/**
	 * Returns the point represented for the port name. The specials names
	 * North, NorthWest, NorthEast, East, West, South, SouthEast and SouthWest.
	 * are accepted. Else, the values accepted follow the pattern
	 * "double,double". where double must be in the range 0..1
	 * 
	 * @param source
	 *            Port Name.
	 * @return point that represent the port value.
	 */
	private static mxPoint portValue(String source) {
		mxPoint fromConstraint = null;

		if (source != null) {
			if (!source.equals("")) {

				if (source.equals("North")) {
					fromConstraint = new mxPoint(0.5, 0);
				} else if (source.equals("South")) {
					fromConstraint = new mxPoint(0.5, 1);

				} else if (source.equals("East")) {
					fromConstraint = new mxPoint(1, 0.5);

				} else if (source.equals("West")) {
					fromConstraint = new mxPoint(0, 0.5);

				} else if (source.equals("NorthWest")) {
					fromConstraint = new mxPoint(0, 0);
				} else if (source.equals("SouthWest")) {
					fromConstraint = new mxPoint(0, 1);
				} else if (source.equals("SouthEast")) {
					fromConstraint = new mxPoint(1, 1);
				} else if (source.equals("NorthEast")) {
					fromConstraint = new mxPoint(1, 0);
				} else {
					try {
						String[] s = source.split(",");
						Double x = Double.valueOf(s[0]);
						Double y = Double.valueOf(s[1]);
						fromConstraint = new mxPoint(x, y);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return fromConstraint;
	}

	/**
	 * Adds the edge represented for the gml edge into the graph with the given
	 * parent.
	 * 
	 * @param graph
	 *            Graph where the vertex will be added.
	 * @param parent
	 *            Parent's cell.
	 * @param edge
	 *            Gml Edge
	 * @return The inserted edge cell.
	 */
	private static mxCell addEdge(mxGraph graph, Object parent, mxGraphMlEdge edge) {
		// Get source and target vertex
		mxPoint fromConstraint = null;
		mxPoint toConstraint = null;
		Object source = cellsMap.get(edge.getEdgeSource());
		Object target = cellsMap.get(edge.getEdgeTarget());

		mxGraphMlData data = dataEdgeKey(edge);

		String style = "";
		String label = "";

		if (data != null) { // Why is the data null for my call graphs?
			mxGraphMlShapeEdge shEdge = data.getDataShapeEdge();
			if (shEdge != null) {
				style = shEdge.getStyle();
				label = shEdge.getText();
			}
		} else {
			HashMap<String, mxGraphMlData> dataMap = edge.getEdgeDataMap();
			style = /*
					 * mxConstants.STYLE_EDGE + "=" + //
					 * mxConstants.EDGESTYLE_TOPTOBOTTOM
					 * mxConstants.EDGESTYLE_ORTHOGONAL + ";" +
					 */ // default
			/*
			 * An edge style just takes into account the source and target
			 * vertices (so it might overlap all other vertices and edges), you
			 * need a global orthogonal edge router, which the Java version
			 * doesn't have.
			 */
			mxConstants.STYLE_STROKEWIDTH + "=" + getStrokeWidth(dataMap) + ";";
			// System.out.println(style);
		}

		// Insert new edge.
		mxCell e = (mxCell) graph.insertEdge(parent, null, label, source, target, style);
		mxICell sourceParent = ((mxCell) source).getParent();
		mxICell targetParent = ((mxCell) target).getParent();
		if (sourceParent != targetParent) { // for parent node's rank
											// computation
			graph.insertEdge(parent, null, null, sourceParent, targetParent,
					// "strokeWidth=0; //still visible, destroys dashing
					mxConstants.STYLE_STROKECOLOR + "=white;" + mxConstants.STYLE_DASHED + "=1;" // invisible
			);
			// graph.getModel().setVisible(edgeParent, false); //does not work
		}
		mxConnectionConstraint ccSource = new mxConnectionConstraint(fromConstraint, false);
		graph.setConnectionConstraint(e, source, true, ccSource);
		graph.setConnectionConstraint(e, target, false, new mxConnectionConstraint(toConstraint, false));

		return e;
	}

	private static String getStrokeWidth(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData frequencyEntry = dataMap.get("frequency");
		if (frequencyEntry != null) {
			return frequencyEntry.getDataValue();
		}
		return "1";
	}

	public String getEdgedefault() {
		return edgedefault;
	}

	public void setEdgedefault(String edgedefault) {
		this.edgedefault = edgedefault;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<mxGraphMlNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<mxGraphMlNode> node) {
		this.nodes = node;
	}

	public List<mxGraphMlEdge> getEdges() {
		return edges;
	}

	public void setEdges(List<mxGraphMlEdge> edge) {
		this.edges = edge;
	}

	/**
	 * Checks if the graph has child nodes or edges.
	 * 
	 * @return Returns <code>true</code> if the graph hasn't child nodes or
	 *         edges.
	 */
	public boolean isEmpty() {
		return nodes.size() == 0 && edges.size() == 0;
	}

	/**
	 * Generates a Key Element from this class.
	 * 
	 * @param document
	 *            Document where the key Element will be inserted.
	 * @return Returns the generated Elements.
	 */
	public Element generateElement(Document document) {
		Element graph = document.createElement(mxGraphMlConstants.GRAPH);

		if (!id.equals("")) {
			graph.setAttribute(mxGraphMlConstants.ID, id);
		}
		if (!edgedefault.equals("")) {
			graph.setAttribute(mxGraphMlConstants.EDGE_DEFAULT, edgedefault);
		}

		for (mxGraphMlNode node : nodes) {
			Element nodeElement = node.generateElement(document);
			graph.appendChild(nodeElement);
		}

		for (mxGraphMlEdge edge : edges) {
			Element edgeElement = edge.generateElement(document);
			graph.appendChild(edgeElement);
		}

		return graph;
	}
}
