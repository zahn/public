package pdt.y.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import pdt.y.model.realizer.MyShapeNodeRealizer;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YDimension;
import y.layout.PortConstraint;
import y.layout.PortConstraintConfigurator;
import y.layout.PortConstraintKeys;
import y.util.Maps;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.Port;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

public class GraphModel {
	private Graph2D graph=new Graph2D();
    
	// Addition data:
	private DataMap dataMap = Maps.createHashedDataMap();
	private DataMap moduleMap = Maps.createHashedDataMap();
	private HierarchyManager hierarchy = null;
	
	private NodeRealizer nodeRealizer;
	private EdgeRealizer edgeRealizer;

	private GroupNodeRealizer groupNodeRealizer;
	
	public GraphModel(){
		initGroupNodeRealizer();
		initNodeRealizer();
		initEdgeNodeRealizer();
	}
	
	private void initGroupNodeRealizer() {
		groupNodeRealizer = new GroupNodeRealizer();
		groupNodeRealizer.setFillColor(Color.GRAY);
		groupNodeRealizer.setShapeType(GroupNodeRealizer.ROUND_RECT);
		groupNodeRealizer.setConsiderNodeLabelSize(true); 
	//	groupNodeRealizer.setAutoBoundsEnabled(true);
	//	Rectangle2D minimalAutoBounds = groupNodeRealizer.getMinimalAutoBounds();
	//	Rectangle2D minimalAutoBounds = groupNodeRealizer.calcMinimumGroupBounds();
		YDimension minSize = groupNodeRealizer.getMinimumSize();
		groupNodeRealizer.setSize(minSize.getWidth(), minSize.getHeight());
	}
	
	private void initNodeRealizer() {
		nodeRealizer = new MyShapeNodeRealizer(this);
		nodeRealizer.setSize(40,40);
		nodeRealizer.setFillColor(Color.ORANGE);      
		graph.setDefaultNodeRealizer(nodeRealizer);
	}

	private void initEdgeNodeRealizer() {
		edgeRealizer = new GenericEdgeRealizer();
		edgeRealizer.setTargetArrow(Arrow.DELTA);
		edgeRealizer.setLineColor(Color.BLUE);
		byte myStyle = LineType.LINE_3.getLineStyle();
		LineType myLineType = LineType.getLineType(2,myStyle);
		edgeRealizer.setLineType(myLineType);
		graph.setDefaultEdgeRealizer(edgeRealizer);
	}

	public String getIdForNode(Node node){
		return (String) dataMap.get(node);
	}
	
	public String getModule(Node node){
		return (String) moduleMap.get(node);
	}
	
	
	
	// Getter and Setter
	
	public NodeRealizer getNodeRealizer() {
		return nodeRealizer;
	}

	public void setNodeRealizer(NodeRealizer nodeRealizer) {
		this.nodeRealizer = nodeRealizer;
		this.graph.setDefaultNodeRealizer(nodeRealizer);
	}

	public EdgeRealizer getEdgeRealizer() {
		return edgeRealizer;
	}

	public void setEdgeRealizer(EdgeRealizer edgeRealizer) {
		this.edgeRealizer = edgeRealizer;
		this.graph.setDefaultEdgeRealizer(edgeRealizer);
	}

	public DataMap getDataMap() {
		return dataMap;
	}

	public void setDataMap(DataMap dataMap) {
		this.dataMap = dataMap;
	}

	public DataMap getModuleMap() {
		return moduleMap;
	}

	public void setModuleMap(DataMap moduleMap) {
		this.moduleMap = moduleMap;
	}

	public Graph2D getGraph() {
		return graph;
	}

	public void setGraph(Graph2D model) {
		this.graph = model;
	}
	
	
	
	public void useHierarchy(){
		if(this.hierarchy == null && this.graph !=null){
			this.hierarchy= new HierarchyManager(graph);
		}
		DefaultHierarchyGraphFactory graphFactory =(DefaultHierarchyGraphFactory)hierarchy.getGraphFactory();
		graphFactory.setDefaultGroupNodeRealizer(groupNodeRealizer);
		graphFactory.setProxyNodeRealizerEnabled(false);
	}
	
	public boolean isHierarchyEnabled(){
		if(hierarchy==null) 
			return false;
		
		return true;
	}
	
	public HierarchyManager getHierarchyManager(){
		return this.hierarchy;
	}
	
	
	public void clear(){
		this.graph.clear();
		
	}
}
