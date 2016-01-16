/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.pdt.graphicalviews.main;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.swing.JPanel;

import org.cs3.pdt.graphicalviews.focusview.CallGraphViewBase;
import org.cs3.pdt.graphicalviews.focusview.ViewBase;
import org.cs3.pdt.graphicalviews.graphml.GraphMLReader;
import org.cs3.pdt.graphicalviews.model.GraphDataHolder;
import org.cs3.pdt.graphicalviews.model.GraphLayout;
import org.cs3.pdt.graphicalviews.model.GraphModel;
import org.cs3.pdt.graphicalviews.view.modes.MoveSelectedSelectionMode;
import org.cs3.pdt.graphicalviews.view.modes.ToggleOpenClosedStateViewMode;
import org.cs3.pdt.graphicalviews.view.modes.WheelScroller;
import org.cs3.prolog.connector.common.Util;
import org.w3c.dom.Document;

import com.mxgraph.io.mxGraphMlCodec;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;

import y.base.Node;
import y.layout.router.OrthogonalEdgeRouter;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.NavigationMode;
import y.view.ViewMode;

public class PDTGraphView extends JPanel {
	final Graph2DView view;
	GraphModel graphModel;
	Graph2D graphY;
	GraphMLReader reader;

	GraphLayout layoutModel;

	EditMode editMode;
	NavigationMode navigationMode;
	Graph2DViewMouseWheelZoomListener wheelZoomListener;
	WheelScroller wheelScroller;

	boolean navigation = false;
	private ViewBase focusView;
	private double xOverhead;
	private double yOverhead;
	private String focusFilePath;

	private static final long serialVersionUID = -611433500513523511L;

	protected void updateView() {
		final mxGraph graph = graphModel.getGraphJ();

		mxGraphComponent graphComponent = createGraphComponent(graph);
		
		add(graphComponent);

		graph.getModel().beginUpdate();
		try {
			resizeCells(graph);
			executeHierarchicalLayout(graph);
			moveChildrenDownAndAdaptRootNodesHeight(graph);
			setRootVerticesDistance(graph);
			normalizeCellCoordinates(graph);
			resetEdges(graph); // to relayout edges by deleting and adding them
			// and computing ports 
			
			graph.getModel().load(focusFilePath);
		} finally {
			mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);
			// layout using morphing: changing (or morphing) one image or shape into
			// another through a seamless transitio
			morph.addListener(mxEvent.DONE, new mxIEventListener() {
				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					graph.getModel().endUpdate();
					// fitViewport();
				}
			});
			morph.startAnimation();
		}
	}
	
	private mxGraphComponent createGraphComponent(mxGraph graph) {
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false); // disable drag and drop edge
		// creation
		// graphComponent.clearCellOverlays(); //does not prevent cells from
		// overlaying labels
		// graphComponent.setSize(50, 50); //does not prevent white space to the
		// left and top
		return graphComponent;
	}

	private void executeHierarchicalLayout(mxGraph graph) {
		// define layout
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		// uneffective:
		// layout.setParentBorder(100); // The border to be added around the
		// children if the parent is to be
		// resized using resizeParent.
		// layout.setResizeParent(true); // Specifies if the parent should
		// be resized after the layout so that it contains all the child cells.
		// layout.setFineTuning(true);
		// resetEdges(graph); //doesnt affect anything
		layout.setDisableEdgeStyle(false);
		layout.execute(graph.getDefaultParent());
	}
	
	private void normalizeCellCoordinates(mxGraph graph) {
		// position the graph in the top left corner of the window!
		computeCoordinatesOverhead(graph);

		Object[] cells = graph.getRootCells();
		graph.getModel().beginUpdate();
		try {
			for (Object c : cells) {
				mxCell cell = (mxCell) c; // cast
				moveCell(graph, cell);
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	private void computeCoordinatesOverhead(mxGraph graph) {
		// The coordinate system in Java is x is positive to the right and y is
		// positive downwards
		double minX = 9999;
		double minY = 9999;
		Object[] cells = graph.getRootCells();
		for (Object c : cells) {
			mxCell cell = (mxCell) c; // cast
			if (cell.isEdge()) continue;
			mxGeometry g = cell.getGeometry();
			double x = g.getX();
			// System.out.println(cell.getValue() + " has x: " + x);
			if (cell.getValue() != null && x < minX) {
				// System.out.println(minX);
				minX = x;
			}
			double y = g.getY();
			// System.out.println(cell.getValue() + " has y: " + y);
			if (cell.getValue() != null && y < minY) {
				// System.out.println(minY);
				minY = y;
			}
		}
		xOverhead = minX - 10;
		yOverhead = minY - 10;
	}
	
	private void moveCell(mxGraph graph, mxCell cell) {
		// The x,y position of a vertex is its position relative to its parent
		// container (graph.getDefaultParent()), so in the case of default
		// grouping (all cells sharing the default parent) the cell positioning
		// is also the absolute co-ordinates on the graph component.

		// System.out.print("cell: " + cell + " with value: " +
		// cell.getValue());
		mxGeometry g = (mxGeometry) cell.getGeometry(); // .clone();
		double newX = g.getX() - xOverhead;
		g.setX(newX);
		double newY = g.getY() - yOverhead; // Move y up
		g.setY(newY);
		// System.out.println(" has reduced x: " + newX);
		// System.out.println(" has reduced y: " + newY);
	}
	
	private void moveCellDownwards(mxGraph graph, mxCell cell) {
		if (cell.isVertex()) {
			mxGeometry g = (mxGeometry) cell.getGeometry();
			double newY = g.getY() + 20; // TODO: replace hard code
			g.setY(newY);
		}
	}

	private void moveChildrenDownAndAdaptRootNodesHeight(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		graph.getModel().beginUpdate();
		try {
			for (Object c : cells) {
				mxCell cell = (mxCell) c; // cast
				int childCount = cell.getChildCount();
				for (int i = 0; i < childCount; i++) {
					mxICell child = cell.getChildAt(i);
					moveCellDownwards(graph, (mxCell) child);
					graph.extendParent(child);
				}
				adaptRootNodeHeight(graph, cell);

			}
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	/**
	 * sets the distance between neighbouring root cells
	 * 
	 * --> to prevent them from overlapping:
	 * 
	 * if neighbours share x- and y-coordinates, move next downwards (increase
	 * y)
	 * 
	 * 
	 * --> to prevent too much empty space between them:
	 * 
	 * if x-neighbours dont share x-coordinates, set the empty space between
	 * them by moving next (leftwards: reduce x)
	 * 
	 * if y-neighbours dont share y-coordinates, set the empty space between
	 * them by moving next (upwards: reduce y)
	 * 
	 * @param graph
	 */
	private void setRootVerticesDistance(mxGraph graph) {
		setRootVerticesYDistance(graph);
		setRootVerticesXDistance(graph);
	}

	/**
	 * sets the x-distance between x-neighbouring root cells
	 * 
	 * --> to prevent too much empty space between them:
	 * 
	 * if x-neighbours dont share x-coordinates, set the empty space between
	 * them by moving next (leftwards: reduce x)
	 * 
	 * @param graph
	 */
	private void setRootVerticesXDistance(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		ArrayList<mxCell> list = getXSortedRootVertices(cells);
		for (int i = 0; i < list.size() - 1; i++) {
			//System.out.println(i);
			mxCell cell = list.get(i);
			Object value = cell.getValue();
			if (value != null && value.equals("preparser.pl")) {
				//System.out.println("setting breakpoint");
			}
			mxCell nextCell = list.get(i + 1);
			if (overlap(cell, nextCell)) {
				//System.out.println(value + " x-overlapping " + nextValue);
				if (setNeighboursXDistance(cell, nextCell)) { 
					//System.out.println(" are set.");
					list = getXSortedRootVertices(cells);
					i--;
				} else {
					//System.out.println(" had already been set.");
				}
			} else if (!sameX(cell, nextCell)) {
				//System.out.println(value + " x-neighboring " + nextValue);
				if (setNeighboursXDistance(cell, nextCell)) {
					//System.out.println("are set.");
					list = getXSortedRootVertices(cells);
					i--;
				} else {
					//System.out.println("had already been set.");
				}
			}
		}
		/*
		 * for (int i = 0; i < cellQueue.size() - 1; i++) { mxCell cell =
		 * cellQueue.poll(); Object value = cell.getValue(); if (value != null
		 * && value.equals("preparser.pl")) { System.out.println(
		 * "setting breakpoint"); } mxCell nextCell = cellQueue.peek(); if
		 * (!sameY(cell, nextCell)) { setNeighboursXDistance(cell, nextCell);
		 * //cellQueue.add(nextCell); } else if (sameX(cell, nextCell)) {
		 * setNeighboursYDistance(cell, nextCell); //cellQueue.add(nextCell); }
		 * }
		 */
	}

	/**
	 * sets the y-distance between y-neighbouring root cells
	 * 
	 * --> to prevent them from overlapping:
	 * 
	 * if neighbours share x- and y-coordinates, move next downwards (increase
	 * y)
	 * 
	 * 
	 * --> to prevent too much empty space between them:
	 * 
	 * if y-neighbours dont share y-coordinates, set the empty space between
	 * them by moving next (upwards: reduce y)
	 * 
	 * @param graph
	 */
	private void setRootVerticesYDistance(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		ArrayList<mxCell> list = getYSortedRootVertices(cells);
		for (int i = 0; i < list.size() - 1; i++) {
			//System.out.println(i);
			mxCell cell = list.get(i);
			mxCell nextCell = list.get(i + 1);
			if (overlap(cell, nextCell)) {
				//System.out.println(value + " y-overlapping " + nextValue);
				if (setNeighboursYDistance(cell, nextCell)) {
					//System.out.println(" are set.");
					list = getYSortedRootVertices(cells);
					i--;
				} else {
					//System.out.println(" had already been set.");
				}
			} else if (!sameY(cell, nextCell)) {
				//System.out.println(value + " y-neighboring " + nextValue);
				if (setNeighboursYDistance(cell, nextCell)) {
					//System.out.println(" are set.");
					list = getYSortedRootVertices(cells);
					i--;
				} else {
					//System.out.println(" had already been set.");
				}
			}
		}
	}

	private boolean overlap(mxCell cell, mxCell nextCell) {
		return sameX(cell, nextCell) && sameY(cell, nextCell);
	}

	private ArrayList<mxCell> getXSortedRootVertices(Object[] cells) {		/*
		 * PriorityQueue<mxCell> cellQueue = new PriorityQueue<mxCell>(10, new
		 * Comparator<mxCell>() { public int compare(mxCell cell1, mxCell cell2)
		 * { Double x1 = cell1.getAbsX(); return x1.compareTo(cell2.getAbsX());
		 * } }); for (Object o : cells) { cellQueue.add((mxCell) o); }
		 */
		ArrayList<mxCell> list = new ArrayList<mxCell>();
		for (Object o : cells) {
			mxCell cell = (mxCell) o;
			if (cell.isVertex()) {
				list.add(cell);
			}
		}
		Collections.sort(list, new Comparator<mxCell>() {
			public int compare(mxCell cell1, mxCell cell2) {
				Double x1 = cell1.getAbsX();
				return x1.compareTo(cell2.getAbsX());
			}
		});
		return list;
	}

	private ArrayList<mxCell> getYSortedRootVertices(Object[] cells) {
		ArrayList<mxCell> list = new ArrayList<mxCell>();
		for (Object o : cells) {
			mxCell cell = (mxCell) o;
			if (cell.isVertex()) {
				list.add((mxCell) o);
			}
		}
		Collections.sort(list, new Comparator<mxCell>() {
			public int compare(mxCell cell1, mxCell cell2) {
				Double y1 = cell1.getAbsY();
				return y1.compareTo(cell2.getAbsY());
			}
		});
		return list;
	}

	private boolean sameX(mxCell cell1, mxCell cell2) {
		double cell1x = cell1.getAbsX();
		double cell2x = cell2.getAbsX();
		if (cell1x < cell2x) {
			// cell1 starts left from cell2
			if (cell1x + cell1.getGeometry().getWidth() < cell2x) {
				// cell1 ends left from cell2
				return false;
			} else {
				return true;
			}
		} else {
			// cell2 starts left from cell2
			if (cell2x + cell2.getGeometry().getWidth() < cell1x) {
				// cell2 ends left from cell1
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean sameY(mxCell cell1, mxCell cell2) {
		double cell1y = cell1.getAbsY();
		double cell2y = cell2.getAbsY();
		if (cell1y < cell2y) {
			// cell1 starts above cell2
			if (cell1y + cell1.getGeometry().getHeight() < cell2y) {
				// cell1 ends above cell2
				return false;
			} else {
				return true;
			}
		} else {
			// cell2 starts above cell2
			if (cell2y + cell2.getGeometry().getHeight() < cell1y) {
				// cell2 ends above cell1
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean setNeighboursXDistance(mxCell left, mxCell right) {
		double leftEnd = left.getAbsX() + left.getGeometry().getWidth();
		double rightStart = leftEnd + 10;
		mxGeometry g = right.getGeometry();
		if (g.getX() == rightStart) {
			return false; // has not been set because it is already correct
		}
		g.setX(rightStart);
		return true;
	}

	private boolean setNeighboursYDistance(mxCell top, mxCell bottom) {
		double topEnd = top.getAbsY() + top.getGeometry().getHeight();
		double downStart = topEnd + 10;
		mxGeometry g = bottom.getGeometry();
		if (g.getY() != downStart) {
			g.setY(downStart);
			return true;
		}
		return false; // has not been set because it is already correct
	}

	private void resetEdges(mxGraph graph) {
		Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_EDGE,
				// mxEdgeStyle.SegmentConnector); //more overlapping
				mxEdgeStyle.OrthConnector);
		Object[] roots = graph.getRootCells();
		Object[] edges = graph.getAllEdges(roots);
		int n = edges.length;
		// System.out.println("Number of edges=" + n);
		for (int i = 0; i < n; i++) {
			mxCell edge = (mxCell) edges[i]; // cast
			// System.out.println("source: "+ edge.getSource().getValue());
			if (graph.getModel().getParent(edge) != null) { // edges will be
															// listed twice as
															// they are referred
															// to in the
				// source vertex and in the target vertex -> check whether it
				// has already been reset
				resetEdge(graph, edge);
			}
		}
	}

	private void resetEdge(mxGraph graph, mxICell edge) {
		// Edges have the concept of control points. These are intermediate
		// points along the edge that the edge is drawn as passing through. The
		// use of control points is sometimes referred to as edge routing.
		Object parent = graph.getDefaultParent();

		if (edge.isEdge()) {
			mxICell source = edge.getTerminal(true);
			mxICell target = edge.getTerminal(false);

			mxICell resetEdge = (mxICell) graph.insertEdge(parent, null, null, source, target, null); // i);
			graph.removeCell(edge);

			String style = edge.getStyle(); // strokeWidth (and edgeStyle)

			double exitX = 1;
			double entryX = 1;
			if (source != target) {
				exitX = computePort(resetEdge, source, true);
				entryX = computePort(resetEdge, target, false);
			}
			style += mxConstants.STYLE_ENTRY_X + "=" + entryX + ";" + mxConstants.STYLE_EXIT_X + "=" + exitX + ";"
					+ mxConstants.STYLE_ENTRY_Y + "=" + "0;" + mxConstants.STYLE_EXIT_Y + "=" + "1;"
					+ mxConstants.STYLE_ENTRY_PERIMETER + "=0;" + mxConstants.STYLE_EXIT_PERIMETER + "=0;"
					;

			resetEdge.setStyle(style); // topToBottom is orthogonal
			//System.out.println(source.getValue() + " to " + target.getValue() + " style: " + style);

			// graph.orderCell(false, (mxCell) edge); //this affects edgeStyle

			// TODO: set edge points next to the vertices they would cross
			// how to find out which vertices they cross?
		}
	}
	
	private double computePort(mxICell resetEdge, mxICell source,
			boolean isSource) {
		source.sortEdges();

		// compute x coordinates by connected vertices' x
		int indexEntry = source.getEdgeIndexSeparated(resetEdge, isSource) + 1;
		// the port should not be 0

		// System.out.println("indexEntry:" + indexEntry + " indexExit:" +
		// indexExit);
		
		int nSource = source.getEdgeCount(isSource) + 1; 
		// the port should not be 1
		
		// System.out.println("nSource:" + nSource + " nTarget:" + nTarget);
		
		double exitX = (double) indexEntry / nSource;
		return exitX;
	}

	private void resizeCells(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		graph.getModel().beginUpdate();
		try {
			for (Object c : cells) {
				mxCell cell = (mxCell) c; // cast
				updateCellSize(graph, cell);
				int childCount = cell.getChildCount();
				for (int i = 0; i < childCount; i++) {
					mxICell child = cell.getChildAt(i);
					updateCellSize(graph, (mxCell) child);
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}

	private void updateCellSize(mxGraph graph, mxCell cell) {
		//System.out.println("cell" + cell.getValue());
		if (cell.isVertex()) {
			graph.updateCellSize(cell);
			// Resize cells' height
			mxGeometry g = (mxGeometry) cell.getGeometry(); // .clone();
			mxRectangle bounds = graph.getView().getState(cell)
					.getLabelBounds();
			double newHeight = bounds.getHeight() + 10;
			g.setHeight(newHeight); // 10 is for padding
		}
	}

	private void adaptRootNodeHeight(mxGraph graph, mxCell cell) {
		if (cell.isVertex()) {
			// Resize cells' height
			mxGeometry g = (mxGeometry) cell.getGeometry(); // .clone();
			double newHeight = g.getHeight() + 10;
			g.setHeight(newHeight);
		}
	}

	public PDTGraphView(ViewBase focusView, String path)
	{
		focusFilePath = path;
		
		setLayout(new BorderLayout());

		this.focusView = focusView;

		layoutModel = new GraphLayout();

		reader = new GraphMLReader();
		view = new Graph2DView();

		initEditMode();

		initNavigationMode();

		initMouseZoomSupport();

		initKeyListener();

		recalculateMode();

		add(view);
	}

	private void initEditMode() {
		editMode = new EditMode();
		editMode.allowNodeCreation(false);
		editMode.allowEdgeCreation(false);
		// editMode.setPopupMode(new HierarchicPopupMode());
		editMode.setMoveSelectionMode(new MoveSelectedSelectionMode(new OrthogonalEdgeRouter()));

		view.addViewMode(editMode);
		view.addViewMode(new ToggleOpenClosedStateViewMode());

	}

	protected void initNavigationMode() {
		navigationMode = new NavigationMode();
		navigationMode.setDefaultCursor(new Cursor(Cursor.MOVE_CURSOR));
		navigationMode.setNavigationCursor(new Cursor(Cursor.MOVE_CURSOR));
	}

	private void initMouseZoomSupport() {
		wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
		wheelScroller = new WheelScroller(view);

		view.getCanvasComponent().addMouseWheelListener(wheelScroller);
	}

	private void initKeyListener() {
		view.getCanvasComponent().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					navigation = calculateMode(navigation, true, focusView.isNavigationModeEnabled());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					navigation = calculateMode(navigation, false, focusView.isNavigationModeEnabled());
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});
	}

	public GraphModel getGraphModel() {
		return graphModel;
	}

	public void recalculateMode() {
		navigation = calculateMode(navigation, false, focusView.isNavigationModeEnabled());
	}

	private boolean calculateMode(boolean isEditorInNavigation, boolean isCtrlPressed, boolean isNavigationModeEnabled) {

		boolean setNavitaionMode = isCtrlPressed ^ isNavigationModeEnabled;

		// If mode was not changed
		if (setNavitaionMode == isEditorInNavigation) {
			return setNavitaionMode;
		}

		if (setNavitaionMode) {
			// Navigation mode
			view.removeViewMode(editMode);
			view.addViewMode(navigationMode);

			view.getCanvasComponent().removeMouseWheelListener(wheelScroller);
			view.getCanvasComponent().addMouseWheelListener(wheelZoomListener);
		}
		else {
			// Edit mode
			view.removeViewMode(navigationMode);
			view.addViewMode(editMode);

			view.getCanvasComponent().removeMouseWheelListener(wheelZoomListener);
			view.getCanvasComponent().addMouseWheelListener(wheelScroller);
		}

		return setNavitaionMode;
	}

	public GraphDataHolder getDataHolder() {
		return graphModel.getDataHolder();
	}

	public Graph2D getGraph2D() {
		return graphY;
	}

	public void addViewMode(ViewMode viewMode) {
		view.addViewMode(viewMode);
	}

	public void setModel(GraphModel model) {
		this.graphModel = model;
	}

	public void loadGraph(File helpFile) {
		loadGraph(reader.readFile(helpFile));
	}

	public void loadGraph(URL resource) {
		loadGraph(reader.readFile(resource));
	}

	public void loadGraph(GraphModel model) {
		graphModel = model;
		if (focusView instanceof CallGraphViewBase)
		{
			CallGraphViewBase callGraphView = (CallGraphViewBase) focusView;
			graphModel.setMetapredicateCallsVisisble(callGraphView.isMetapredicateCallsVisible());
			graphModel.setInferredCallsVisible(callGraphView.isInferredCallsVisible());
		}
		graphModel.categorizeData();
		graphModel.assignPortsToEdges();
		graphY = graphModel.getGraph();
		view.setGraph2D(graphY);

		updateView();
	}

	protected void updateViewY() {
		for (Node node : graphY.getNodeArray()) {
			String labelText = graphModel.getLabelTextForNode(node);
			graphY.setLabelText(node, labelText);
		}

		calcLayout();
	}

	public boolean isEmpty() {
		return this.graphModel// .getGraphJ() //NullPointerException
				== null;
		//return graphY == null
				//|| graphY.getNodeArray().length == 0;
	}

	public void calcLayout() {
		view.applyLayout(layoutModel.getLayouter());

		// layoutModel.getLayouter().doLayout(graph);
		// graph.updateViews();

		view.fitContent();
		view.updateView();
	}

	public void updateLayout() {
		if (Util.isMacOS()) {
			view.applyLayout(layoutModel.getLayouter());
		} else {
			view.applyLayoutAnimated(layoutModel.getLayouter());
		}
		view.fitContent();
		view.updateView();
	}

}
