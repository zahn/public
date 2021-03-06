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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.swing.JLabel;

import org.cs3.pdt.graphicalviews.focusview.CallGraphViewBase;
import org.cs3.pdt.graphicalviews.focusview.ViewBase;
import org.cs3.pdt.graphicalviews.model.GraphModel;
import org.cs3.pdt.graphicalviews.model.realizer.edges.CallEdgeRealizer;
import org.cs3.pdt.graphicalviews.model.realizer.edges.LoadEdgeRealizer;
import org.cs3.pdt.graphicalviews.model.realizer.edges.LogtalkEdgeRealizer;

import y.base.Edge;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class PDTGraphViewJ extends PDTGraphView {

	private static final long serialVersionUID = -611433500513523511L;

	private String focusFilePath;

	private double xOverhead;
	private double yOverhead;

	private JLabel label;

	private mxGraphComponent graphComponent;

	public PDTGraphViewJ(ViewBase focusView, String path) {
		super(focusView);
		this.focusFilePath = path;

		label = new JLabel();
		label.setBounds(5, 5, 150, 20);
		// textField.setEditable(false);
		label.setVisible(false);

		add(label);
		setComponentZOrder(label, 0);
	}

	public boolean isEmpty() {
		return this.graphModel// .getGraphJ() //NullPointerException
				== null;
		// return graphY == null
		// || graphY.getNodeArray().length == 0;
	}

	public void loadGraph(File helpFile) {
		loadGraph(reader.readFile(helpFile));
	}

	public void loadGraph(GraphModel model) {
		graphModel = model;

		if (focusView instanceof CallGraphViewBase) {
			CallGraphViewBase callGraphView = (CallGraphViewBase) focusView;
			graphModel.setMetapredicateCallsVisible(callGraphView.isMetapredicateCallsVisible());
			graphModel.setInferredCallsVisible(callGraphView.isInferredCallsVisible());
		}
		//graphModel.categorizeData();
		//graphModel.assignPortsToEdges();

		// private void categorizeEdges() {
		/*
		 * for (Edge edge: graph.getEdgeArray()) { if (dataHolder.isLoadingEdge(edge)) { LoadEdgeRealizer newLoadEdgeRealizer = new LoadEdgeRealizer(loadEdgeRealizer); graph.setRealizer(edge, newLoadEdgeRealizer);
		 * 
		 * String edgeLabel = dataHolder.getEdgeLabel(edge); if (edgeLabel != null && !edgeLabel.isEmpty()) { newLoadEdgeRealizer.setLabelText(edgeLabel); }
		 * 
		 * } else if (dataHolder.isCallEdge(edge)) { boolean isMetaCall = dataHolder.isMetaCall(edge); boolean isDatabaseCall = dataHolder.isDatabaseCall(edge);
		 * 
		 * CallEdgeRealizer newCallEdgeRealizer = new CallEdgeRealizer(callEdgeRealizer, isMetaCall, isDatabaseCall); graph.setRealizer(edge, newCallEdgeRealizer); newCallEdgeRealizer.adjustLineWidth(this);
		 * 
		 * // String label = dataHolder.getEdgeLabel(edge); // newCallEdgeRealizer.setLabelText(label);
		 * 
		 * boolean isMetaPredicateCall = dataHolder.isMetaPred(edge.target()); boolean isInferredCall = isMetaCall || isDatabaseCall;
		 * 
		 * newCallEdgeRealizer.setVisible(!isMetaPredicateCall && !isInferredCall || isMetaPredicateCall && metapredicateCallsVisisble || isInferredCall && inferredCallsVisible); } else if (dataHolder.isLogtalkGraphEdge(edge)){ LogtalkEdgeRealizer
		 * logtalkEdgeRealizer = new LogtalkEdgeRealizer(); graph.setRealizer(edge, logtalkEdgeRealizer); logtalkEdgeRealizer.init(this); } else { // no realizer to set because it is already bound to default realizer } } }
		 */

		updateView();
	}

	private void adaptRootNodeHeight(mxGraph graph, mxCell cell) {
		if (cell.isVertex()) {
			// Resize cells' height
			mxGeometry g = (mxGeometry) cell.getGeometry(); // .clone();
			double newHeight = g.getHeight() + 10;
			g.setHeight(newHeight);
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
			if (cell.isEdge())
				continue;
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
		yOverhead = minY - 20;
	}

	private mxGraphComponent createGraphComponent(mxGraph graph) {
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false); // disable drag and drop edge
		// creation
		// graphComponent.clearCellOverlays(); //does not prevent cells from
		// overlaying labels
		// graphComponent.setSize(50, 50); //does not prevent white space to the
		// left and top
		graphComponent.setToolTips(true);
		new mxKeyboardHandler(graphComponent); // to enable resetting edges by (Fn) DEL
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
				graph.resetEdge(edge);
			}
		}
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

	/**
	 * sets the x-distance between x-neighbouring root cells y --> to prevent too much empty space between them:
	 * 
	 * if x-neighbours dont share x-coordinates, set the empty space between them by moving next (leftwards: reduce x)
	 * 
	 * if y-neighbours dont share y-coordinates, set the empty space between them by moving next (upwards: reduce y)
	 */
	private void setRootVerticesXDistance(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		ArrayList<mxCell> list = sortByX(cells);
		setVerticesXDistance(list);
		/*
		 * for (int i = 0; i < cellQueue.size() - 1; i++) { mxCell cell = cellQueue.poll(); Object value = cell.getValue(); if (value != null && value.equals("preparser.pl")) { System.out.println( "setting breakpoint"); } mxCell nextCell =
		 * cellQueue.peek(); if (!sameY(cell, nextCell)) { setNeighboursXDistance(cell, nextCell); //cellQueue.add(nextCell); } else if (sameX(cell, nextCell)) { setNeighboursYDistance(cell, nextCell); //cellQueue.add(nextCell); } }
		 */
	}

	private void setChildVerticesXDistance(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		/*
		 * for (int i = 0; i < cellQueue.size() - 1; i++) { mxCell cell = cellQueue.poll(); Object value = cell.getValue(); if (value != null && value.equals("preparser.pl")) { System.out.println( "setting breakpoint"); } mxCell nextCell =
		 * cellQueue.peek(); if (!sameY(cell, nextCell)) { setNeighboursXDistance(cell, nextCell); //cellQueue.add(nextCell); } else if (sameX(cell, nextCell)) { setNeighboursYDistance(cell, nextCell); //cellQueue.add(nextCell); } }
		 */
		for (int i = 0; i < cells.length; i++) {
			setChildVerticesXDistance((mxCell) cells[i]);
		}
	}

	private void setChildVerticesXDistance(mxCell parent) {
		int n = parent.getChildCount();
		Object[] cells = new Object[n];
		for (int i = 0; i < n; i++) {
			cells[i] = parent.getChildAt(i);
		}
		ArrayList<mxCell> list = sortByX(cells);

		setVerticesXDistance(list);
		/*
		 * for (int i = 0; i < cellQueue.size() - 1; i++) { mxCell cell = cellQueue.poll(); Object value = cell.getValue(); if (value != null && value.equals("preparser.pl")) { System.out.println( "setting breakpoint"); } mxCell nextCell =
		 * cellQueue.peek(); if (!sameY(cell, nextCell)) { setNeighboursXDistance(cell, nextCell); //cellQueue.add(nextCell); } else if (sameX(cell, nextCell)) { setNeighboursYDistance(cell, nextCell); //cellQueue.add(nextCell); } }
		 */
	}

	/**
	 * @param list
	 */
	private void setVerticesXDistance(ArrayList<mxCell> list) {
		boolean test = false;
		/*
		 * if (list.size() > 0) { String value = (String) list.get(0).getParent().getValue(); if (value != null && value.equals("timetable.pl")) { test = true; } }
		 */
		Object[] cells;
		cells = list.toArray();
		list = sortByX(cells);
		for (int i = 0; i < list.size() - 1; i++) {
			if (test)
				System.out.println(i);
			mxCell cell = list.get(i);
			mxCell nextCell = list.get(i + 1);
			if (overlap(cell, nextCell)) {
				if (test)
					System.out.println(cell.getValue() + " x-overlapping "
							+ nextCell.getValue());
				if (setNeighboursXDistance(cell, nextCell)) {
					list = sortByX(cells);
					i--;
					if (i >= 0)
						i--;
				}
			} else if (!sameX(cell, nextCell)) {
				if (test)
					System.out.println(cell.getValue() + " x-neighboring "
							+ nextCell.getValue());
				if (setNeighboursXDistance(cell, nextCell)) {
					list = sortByX(cells);
					i--;
					if (i >= 0)
						i--;
				}
			}
			if (test)
				System.out.println(cell.getGeometry().getX() + " :x: "
						+ nextCell.getGeometry().getX());
		}
	}

	private boolean overlap(mxCell cell, mxCell nextCell) {
		return sameX(cell, nextCell) && sameY(cell, nextCell);
	}

	private ArrayList<mxCell> sortByX(Object[] cells) {
		/*
		 * PriorityQueue < mxCell > cellQueue = new PriorityQueue < mxCell >(10, new Comparator < mxCell >() { public int compare ( mxCell cell1 , mxCell cell2 ) { Double x1 = cell1 . getAbsX (); return x1. compareTo ( cell2 . getAbsX ()); } }); for
		 * (Object o : cells ) { cellQueue . add(( mxCell ) o); }
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

	private ArrayList<mxCell> sortByY(Object[] cells) {
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

	/**
	 * if right's x is too right, then move it to the left. if it is connected to other edges then only move it the half way and move left to the right the other half such that they meet in the middle.
	 * 
	 * @param left
	 *            cell
	 * @param right
	 *            cell = cell with the smallest x which is bigger than left cell's x
	 * @return true if right has been moved to the left. false if nothing has been changed.
	 */
	private boolean setNeighboursXDistance(mxCell left, mxCell right) {
		boolean test = false;
		String value = (String) left.getParent().getValue();
		if (value != null && value.equals("available.pl")) {
			// test = true;
		}
		double toleratedDistance = 100; // tolerated distance between 2 nodes
		mxGeometry gLeft = left.getGeometry();
		mxGeometry gRight = right.getGeometry();
		double startLeft = gLeft.getX();
		double startRight = gRight.getX();
		double width = gLeft.getWidth();
		double endLeft = startLeft + width;
		if (startRight <= endLeft + toleratedDistance) {
			if (test)
				System.out.println(" are ok.");
			return false;
		}
		// move:
		double optDistance = 10; // optimal distance between 2 nodes
		if (right.getEdgeCount() > 0) {
			double halfDistance = ((endLeft + optDistance - startRight) / 2);
			double middle = endLeft - halfDistance;
			gRight.setX(middle + optDistance);
			gLeft.setX(middle - width);
		} else {
			gRight.setX(endLeft + optDistance);
		}
		if (test) {
			System.out.println("are setting right from " + startRight + " to "
					+ right.getGeometry().getX());
			System.out.println("and left from " + startLeft + " to "
					+ left.getGeometry().getX());
			System.out.println("width: " + left.getGeometry().getWidth());
			System.out.println("startRight: " + startRight);
		}
		return true;
	}

	/**
	 * sets the y-distance between y-neighbouring root cells
	 * 
	 * --> to prevent them from overlapping:
	 * 
	 * if neighbours share x- and y-coordinates, move next downwards (increase y)
	 * 
	 * 
	 * --> to prevent too much empty space between them:
	 * 
	 * if y-neighbours dont share y-coordinates, set the empty space between them by moving next (upwards: reduce y)
	 * 
	 * @param graph
	 */
	private void setRootVerticesYDistance(mxGraph graph) {
		Object[] cells = graph.getRootCells();
		ArrayList<mxCell> list = sortByY(cells);
		for (int i = 0; i < list.size() - 1; i++) {
			// System.out.println(i);
			mxCell cell = list.get(i);
			mxCell nextCell = list.get(i + 1);
			if (overlap(cell, nextCell)) {
				// System.out.println(value + " y-overlapping " + nextValue);
				if (setNeighboursYDistance(cell, nextCell)) {
					// System.out.println(" are set.");
					list = sortByY(cells);
					i--;
				} else {
					// System.out.println(" had already been set.");
				}
			} else if (!sameY(cell, nextCell)) {
				// System.out.println(cell.getValue() + " y-neighboring "
				// + nextCell.getValue());
				// additional check to not break the hierarchy:
				if (i == 0 || !sameY(nextCell, list.get(i - 1))) {
					if (setNeighboursYDistance(cell, nextCell)) {
						// System.out.println(" are set.");
						list = sortByY(cells);
						i--;
					} else {
						// System.out.println(" had already been set.");
					}
				}
			}
		}
	}

	private boolean updateCellSize(mxGraph graph, mxCell cell) {
		// System.out.println("cell" + cell.getValue());
		if (cell.isVertex()) {
			graph.updateCellSize(cell);
			// Resize cells' height
			mxGeometry g = (mxGeometry) cell.getGeometry(); // .clone();
			mxGraphView graphView = graph.getView();
			mxCellState state = graphView.getState(cell);
			if (state == null) {
				return false;
			}
			mxRectangle bounds = state.getLabelBounds();
			double newHeight = bounds.getHeight() + 10;
			g.setHeight(newHeight); // 10 is for padding
		}
		return true;

	}

	protected void updateView() {
		mxGraph graph = graphModel.getGraphJ();

		if (graphComponent == null) {
			graphComponent = createGraphComponent(graph);
			add(graphComponent);
		} else {
			graphComponent.setGraph(graph);
			this.validate();
		}

		graph.getModel().beginUpdate();
		try {
			resizeCells(graph);
			executeHierarchicalLayout(graph);

			setRootVerticesXDistance(graph);
			setChildVerticesXDistance(graph);
			graph.updateGroupBounds(graph.getRootCells(), 10, true);
			moveChildrenDownAndAdaptRootNodesHeight(graph);
			setRootVerticesYDistance(graph);

			normalizeCellCoordinates(graph);
			resetEdges(graph); // to relayout edges by deleting and adding them
			// and computing ports

			hideEdges(graph, this.graphModel.isInferredCallsVisible(), this.graphModel.isMetapredicateCallsVisible());

			// graph.getModel().load(focusFilePath);
		} finally {
			graph.getModel().endUpdate();
		}

		graph.getModel().addLabel(label);
	}

	private void hideEdges(mxGraph graph, boolean isInferredCallsVisible,
			boolean isMetaCallsVisible) {
		Object[] roots = graph.getRootCells();
		Object[] edges = graph.getAllEdges(roots);
		int n = edges.length;
		// System.out.println("Number of edges=" + n);
		for (int i = 0; i < n; i++) {
			mxCell edge = (mxCell) edges[i]; // cast
			if (!edge.isVisible()) {
				continue; //nothing to hide
			}
			// System.out.println("source: "+ edge.getSource().getValue());

			mxCell target = (mxCell) edge.getTarget();
			String metadata = edge.getAttribute("metadata");
			//String isMetaPredicate = target.getAttribute("isMetaPredicate");
			boolean isMetaPredicate = target.getStyle().contains("grey");
			// System.out.println("metadata: " + metadata);
			if (metadata == null) {
				continue;
			}
			if (metadata.equals("database") || metadata.equals("metacall")) {
				edge.setVisible(isInferredCallsVisible);
			}
			//if (isMetaPredicate != null && isMetaPredicate.equals("true")) {
			if (isMetaPredicate) {
				edge.setVisible(isMetaCallsVisible);
			}

		}
	}


}
