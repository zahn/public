/**
 * Copyright (c) 2010 David Benson, Gaudenz Alder
 */
package com.mxgraph.io.graphml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.TerminalNotFoundException;
import com.mxgraph.view.mxConnectionConstraint;
import com.mxgraph.view.mxGraph;

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
		this.edgedefault = graphElement
				.getAttribute(mxGraphMlConstants.EDGE_DEFAULT);

		// Adds node elements
		List<Element> nodeElements = mxGraphMlUtils.childsTags(graphElement,
				mxGraphMlConstants.NODE);

		for (Element nodeElem : nodeElements) {
			mxGraphMlNode node = new mxGraphMlNode(nodeElem);

			nodes.add(node);
		}

		// Adds edge elements
		List<Element> edgeElements = mxGraphMlUtils.childsTags(graphElement,
				mxGraphMlConstants.EDGE);

		for (Element edgeElem : edgeElements) {
			mxGraphMlEdge edge = new mxGraphMlEdge(edgeElem);

			if (edge.getEdgeDirected().equals("")) {
				if (edgedefault.equals(mxGraphMlConstants.EDGE_DIRECTED)) {
					edge.setEdgeDirected("true");
				} else if (edgedefault
						.equals(mxGraphMlConstants.EDGE_UNDIRECTED)) {
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
		// savedCells = graph.getModel().load();

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
			try {
				addEdge(graph, parent, edge);
			} catch (TerminalNotFoundException e) {
				// TODO Auto-generated catch block
			}
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
		HashMap<String, mxGraphMlKey> keyMap = mxGraphMlKeyManager
				.getInstance().getKeyMap(); // hello:
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
		HashMap<String, mxGraphMlKey> keyMap = mxGraphMlKeyManager
				.getInstance().getKeyMap();
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
			v1 = (mxCell) graph.insertVertex(parent, id, label, x, y, w, h,
					style);
		} else {
			HashMap<String, mxGraphMlData> dataMap = node.getNodeDataMap();
			String pdtId = getPdtId(dataMap);

			String label = getLabel(dataMap);
			String style = // mxConstants.STYLE_STARTSIZE + "=10;" + no effect
			// mxConstants.STYLE_SWIMLANE_FILLCOLOR + "=orange;" + no effect
			mxConstants.STYLE_OVERFLOW + "=hidden;" + mxConstants.STYLE_SPACING
					+ "=5;" + mxConstants.STYLE_FOLDABLE + "=0;"
					+ mxConstants.STYLE_FONTCOLOR + "=black;"
					+ mxConstants.STYLE_LABEL_BACKGROUNDCOLOR + "="
					+ getHeaderColor(dataMap) + ";"
					+ mxConstants.STYLE_VERTICAL_ALIGN + "="
					+ mxConstants.ALIGN_TOP + ";"
					+ mxConstants.STYLE_STROKECOLOR + "="
					+ getBorderColor(dataMap) + ";" + mxConstants.STYLE_DASHED
					+ "=" + isDashed(dataMap) + ";"
					+ mxConstants.STYLE_FILLCOLOR + "=" + getFillColor(dataMap)
					+ ";" + mxConstants.STYLE_SHAPE + "=" + getShape(dataMap);
			v1 = (mxCell) graph.insertVertex(parent, id, label, 0, 0, 100, 100,
					style, pdtId);
			// v1.setPdtId(pdtId); //too late

			String toolTip = getToolTip(dataMap);
			v1.setToolTip(toolTip);
		}

		// loadGeometry(v1); //if we load the geometry now, the layouters will
		// overwrite it

		cellsMap.put(id, v1);
		List<mxGraphMlGraph> graphs = node.getNodeGraph();

		for (mxGraphMlGraph gmlGraph : graphs) {
			gmlGraph.addGraph(graph, v1);
		}
		return v1;
	}

	private String getToolTip(HashMap<String, mxGraphMlData> dataMap) {
		StringBuilder sb = new StringBuilder();
		/*
		 * if (isModule(dataMap)) { sb.append("Module: "); } else if
		 * (isFile(dataMap)) { sb.append("File: "); } else if
		 * (isPredicate(dataMap)) { sb.append("Predicate: "); }
		 */// not shown

		sb.append(getLabel(dataMap));

		if (isExported(dataMap)) {
			sb.append(" [Exported]");
		}

		if (isDynamicNode(dataMap)) {
			sb.append(" [Dynamic]");
		}

		if (isUnusedLocal(dataMap)) {
			sb.append(" [Unused]");
		}

		if (isInferredPredicate(dataMap)) {
			sb.append(" [Inferred]");
		}

		if (isInferredCall(dataMap)) {
			sb.append(getLabel(dataMap)); // aspect_action(...,...,grossvater(...,...))
		}

		/*
		 * if ("inferred".equals(getMetaPredType(dataMap))) {
		 * setLabelText(model.getLabelTextForNode() + " [Inferred]"); }
		 */
		return sb.toString();
	}
	
	private static boolean isInferredCall(HashMap<String, mxGraphMlData> dataMap) {
		return isMetaCall(dataMap) || isDatabaseCall(dataMap);
	}

	private static boolean isMetaCall(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData metadataEntry = dataMap.get("metadata");
		if (metadataEntry != null) {
			if (metadataEntry.getDataValue().equals("metacall")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDatabaseCall(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData metadataEntry = dataMap.get("metadata");
		if (metadataEntry != null) {
			if (metadataEntry.getDataValue().equals("database")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isFile(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("file")) {
			return true;
		}
		return false;
	}

	private boolean isModule(HashMap<String, mxGraphMlData> dataMap) {
		if (isFile(dataMap)) {
			mxGraphMlData moduleEntry = dataMap.get("module");
			if (moduleEntry != null) {
				String moduleValue = moduleEntry.getDataValue();
				if (!moduleValue.equals("user")) {
					return true;
				}
			}
		}
		return false;
	}

	private String getHeaderColor(HashMap<String, mxGraphMlData> dataMap) {
		if (isModule(dataMap)) {
			return "grey";
		}
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("file")) {
			return "white";
		}
		return "none";
	}

	private boolean isPredicate(HashMap<String, mxGraphMlData> dataMap) {
		String kindValue = dataMap.get("kind").getDataValue();
		if (kindValue.equals("predicate")) {
			return true;
		}
		return false;
	}
	

	private boolean isInferredPredicate(HashMap<String, mxGraphMlData> dataMap) {
		if (isPredicate(dataMap)) {
			mxGraphMlData isUnusedLocalEntry = dataMap.get("metaPredicateType");
			if (isUnusedLocalEntry != null) {
				String isUnusedLocalValue = isUnusedLocalEntry.getDataValue();
				if (isUnusedLocalValue.equals("inferred")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isMetaPredicate(HashMap<String, mxGraphMlData> dataMap) {
		if (isPredicate(dataMap)) {
			mxGraphMlData isUnusedLocalEntry = dataMap.get("isMetaPredicate");
			if (isUnusedLocalEntry != null) {
				String isUnusedLocalValue = isUnusedLocalEntry.getDataValue();
				if (isUnusedLocalValue.equals("true")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isUnusedLocal(HashMap<String, mxGraphMlData> dataMap) {
		if (isPredicate(dataMap)) {
			mxGraphMlData isUnusedLocalEntry = dataMap.get("isUnusedLocal");
			if (isUnusedLocalEntry != null) {
				String isUnusedLocalValue = isUnusedLocalEntry.getDataValue();
				if (isUnusedLocalValue.equals("true")) {
					return true;
				}
			}
		}
		return false;
	}
	

	private String getShape(HashMap<String, mxGraphMlData> dataMap) {
		if (isMetaPredicate(dataMap)) {
			return mxConstants.SHAPE_HEXAGON;
		}
		if (isTransparent(dataMap)) {
			return mxConstants.SHAPE_ELLIPSE;
		}
		return mxConstants.SHAPE_RECTANGLE;
	}

	private String getBorderColor(HashMap<String, mxGraphMlData> dataMap) {
		if (isUnusedLocal(dataMap)) {
			return "red";
		}
		if (isMetaPredicate(dataMap)) {
			return "grey";
		}
		return "black";
	}

	private boolean isDynamicNode(HashMap<String, mxGraphMlData> dataMap) {
		if (isPredicate(dataMap)) {
			mxGraphMlData isDynamicEntry = dataMap.get("isDynamic");
			if (isDynamicEntry != null) {
				String isDynamicValue = isDynamicEntry.getDataValue();
				if (isDynamicValue.equals("true")) {
					return true;
				}
			}
		}
		return false;
	}

	private String isDashed(HashMap<String, mxGraphMlData> dataMap) {
		if (isDynamicNode(dataMap)) {
			return "1";
		} else {
			return "0";
		}
	}

	private boolean isTransparent(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData isTransparentEntry = dataMap.get("isTransparent");
		if (isTransparentEntry != null) {
			String isTransparentValue = isTransparentEntry.getDataValue();
			if (isTransparentValue.equals("true")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isExported(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData isExportedEntry = dataMap.get("isExported");
		if (isExportedEntry != null) {
			String isExportedValue = isExportedEntry.getDataValue();
			if (isExportedValue.equals("true")) {
				return true;
			}
		}
		return false;
	}

	private String getFillColor(HashMap<String, mxGraphMlData> dataMap) {
		if (!isPredicate(dataMap)) {
			return "white";
		}
		if (isExported(dataMap)) {
			return "#90EE90"; //light green
		}
		return "yellow";
	}

	private static String getLabel(HashMap<String, mxGraphMlData> dataMap) {
		String label = "";
		String arity = "";
		mxGraphMlData dataEntry = dataMap.get("value");
		if (dataEntry == null) {
			dataEntry = dataMap.get("label");
		}
		if (dataEntry == null) {
			dataEntry = dataMap.get("functor");
			mxGraphMlData arityEntry = dataMap.get("arity");
			if (arityEntry != null) {
				arity = " / " + arityEntry.getDataValue();
			}
		}
		if (dataEntry != null) {
			label = dataEntry.getDataValue() + arity;
		}
		return label;
	}

	private String getPdtId(HashMap<String, mxGraphMlData> dataMap) {
		String label = "";
		mxGraphMlData dataEntry = dataMap.get("id"); // "pdtId" ?
		if (dataEntry != null) {
			label = dataEntry.getDataValue();
		}
		return label;
	}

	// this function is not used anymore because of a custom implementation
	// /**
	// * Returns the point represented for the port name. The specials names
	// * North, NorthWest, NorthEast, East, West, South, SouthEast and
	// SouthWest.
	// * are accepted. Else, the values accepted follow the pattern
	// * "double,double". where double must be in the range 0..1
	// *
	// * @param source
	// * Port Name.
	// * @return point that represent the port value.
	// */
	// private static mxPoint portValue(String source) {
	// mxPoint fromConstraint = null;
	//
	// if (source != null) {
	// if (!source.equals("")) {
	//
	// if (source.equals("North")) {
	// fromConstraint = new mxPoint(0.5, 0);
	// } else if (source.equals("South")) {
	// fromConstraint = new mxPoint(0.5, 1);
	//
	// } else if (source.equals("East")) {
	// fromConstraint = new mxPoint(1, 0.5);
	//
	// } else if (source.equals("West")) {
	// fromConstraint = new mxPoint(0, 0.5);
	//
	// } else if (source.equals("NorthWest")) {
	// fromConstraint = new mxPoint(0, 0);
	// } else if (source.equals("SouthWest")) {
	// fromConstraint = new mxPoint(0, 1);
	// } else if (source.equals("SouthEast")) {
	// fromConstraint = new mxPoint(1, 1);
	// } else if (source.equals("NorthEast")) {
	// fromConstraint = new mxPoint(1, 0);
	// } else {
	// try {
	// String[] s = source.split(",");
	// Double x = Double.valueOf(s[0]);
	// Double y = Double.valueOf(s[1]);
	// fromConstraint = new mxPoint(x, y);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	// return fromConstraint;
	// }

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
	 * @throws TerminalNotFoundException 
	 */
	private mxCell addEdge(mxGraph graph, Object parent,
			mxGraphMlEdge edge) throws TerminalNotFoundException {
		// Get source and target vertex
		mxPoint fromConstraint = null;
		mxPoint toConstraint = null;
		Object source = cellsMap.get(edge.getEdgeSource());
		Object target = cellsMap.get(edge.getEdgeTarget());

		String style = "";
		String label = "";
		String toolTip = "";
		String metadata = "";
		
		mxGraphMlData data = dataEdgeKey(edge);
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
					 */// default
			/*
			 * An edge style just takes into account the source and target
			 * vertices (so it might overlap all other vertices and edges), you
			 * need a global orthogonal edge router, which the Java version
			 * doesn't have.
			 */
			mxConstants.STYLE_STROKEWIDTH + "=" + getStrokeWidth(dataMap) + ";"
					/*
					 * + mxConstants.STYLE_DASH_PATTERN + "=" +
					 * mxConstants.DEFAULT_DASHED_PATTERN + ";" +
					 * mxConstants.STYLE_DASHED + "=1;"
					 */// buggy default dashed pattern
					+ mxConstants.STYLE_STROKECOLOR + "="
					+ getEdgeColor(dataMap) + ";";
			// System.out.println(style);

			toolTip = getLabel(dataMap);
			metadata = getMetadata(dataMap);
		}
		// Insert new edge.
		mxCell e = (mxCell) graph.insertEdge(parent, null, label, source,
				target, style);
		e.setToolTip(toolTip);
		//e.setValue(new SerializableIIOMetadataNode());
		Document doc = mxDomUtils.createDocument();
		e.setValue(doc.createElement(mxGraphMlConstants.EDGE));
		e.setAttribute("metadata", metadata);
		insertParentsEdge(graph, parent, source, target);
		mxConnectionConstraint ccSource = new mxConnectionConstraint(
				fromConstraint, false);
		graph.setConnectionConstraint(e, source, true, ccSource);
		graph.setConnectionConstraint(e, target, false,
				new mxConnectionConstraint(toConstraint, false));

		return e;
	}

	/**
	 * @param graph
	 * @param parent
	 * @param source
	 * @param target
	 */
	private static void insertParentsEdge(mxGraph graph, Object parent,
			Object source, Object target) {
		mxICell sourceParent = ((mxCell) source).getParent();
		mxICell targetParent = ((mxCell) target).getParent();
		if (sourceParent != targetParent) { // for parent node's rank
											// computation
			mxCell edge = (mxCell) graph.insertEdge(parent, null, null, sourceParent, targetParent, "");
			
			edge.setVisible(false);
		}
	}

	private static String getEdgeColor(HashMap<String, mxGraphMlData> dataMap) {
		if (isInferredCall(dataMap)) {
			return "grey";
		}
		return "black";
	}

	private static String getStrokeWidth(HashMap<String, mxGraphMlData> dataMap) {
		return getFrequency(dataMap);
	}

	/**
	 * @param dataMap
	 * @return
	 */
	private static String getFrequency(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData frequencyEntry = dataMap.get("frequency");
		if (frequencyEntry != null) {
			return frequencyEntry.getDataValue();
		}
		return "1";
	}
	
	private static String getMetadata(HashMap<String, mxGraphMlData> dataMap) {
		mxGraphMlData metadata = dataMap.get("metadata");
		if (metadata != null) {
			return metadata.getDataValue();
		}
		return "";
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
