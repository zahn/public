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

	private static final long serialVersionUID = -611433500513523511L;

	protected void updateView() {
		final mxGraph graph = graphModel.getGraphJ();
		graph.setAllowDanglingEdges(false);

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false); // disable drag and drop edge creation
		
		add(graphComponent);

		graph.getModel().beginUpdate();
		try {
			resizeCells(graph);

			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
			layout.setDisableEdgeStyle(false);
			layout.execute(graph.getDefaultParent());

			// to prevent overlapping root node labels
			moveChildrenDownAndAdaptRootNodesHeight(graph);

			resetEdges(graph); // to relayout edges by deleting and adding them

			Map<String, Object> EdgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
			EdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
			EdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.SegmentConnector);

			mxOrganicLayout edgeLayout = new mxOrganicLayout(graph);
			edgeLayout.setDisableEdgeStyle(false);
			edgeLayout.execute(graph.getDefaultParent());
		} finally {
			mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

			morph.addListener(mxEvent.DONE, new mxIEventListener() {

				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					graph.getModel().endUpdate();
				}

			});

			morph.startAnimation();
		}
	}

	private void moveCellDownwards(mxGraph graph, mxCell cell) {
		if (cell.isVertex()) {
			mxGeometry g = (mxGeometry) cell.getGeometry();
			double newY = g.getY() + 20; // TODO: replace hard code
			g.setY(newY);
		}
	}

	private void moveChildrenDownAndAdaptRootNodesHeight(mxGraph graph) {
		Object[] cells = graph.getCells();
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

	private void resetEdges(mxGraph graph) {
		Object[] cells = graph.getCells();
		try {
			ArrayList<Object> oldEdges = new ArrayList<Object>();
			for (Object c : cells) {
				mxCell cell = (mxCell) c; // cast
				int childCount = cell.getChildCount();
				for (int i = 0; i < childCount; i++) {
					mxICell child = cell.getChildAt(i);
					resetEdge(graph, child, oldEdges);
				}
				resetEdge(graph, cell, oldEdges);
			}
			graph.removeCells(oldEdges.toArray());
		} finally {
		}
	}

	private void resetEdge(mxGraph graph, mxICell edge,
			ArrayList<Object> oldEdges) {
		if (edge.isEdge()) {
			oldEdges.add(edge);

			mxICell source = edge.getTerminal(true);
			mxICell target = edge.getTerminal(false);

			graph.insertEdge(
					graph.getDefaultParent(), null, null, source, target);
		}
	}

	private void resizeCells(mxGraph graph) {
		Object[] cells = graph.getCells();
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
		System.out.println("cell" + cell.getValue());
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

	public PDTGraphView(ViewBase focusView)
	{
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
		return graphY == null
				|| graphY.getNodeArray().length == 0;
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
