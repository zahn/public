/*
 * ld: the contents of this file where copied from the jdt source,
 * so i left the below copyright notice intact.
 */

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.cs3.pdt.internal.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * TODO: this class should replace JavaSearchTableContentProvider
 * (must generalize type of fResult to AbstractTextSearchResult in JavaSearchContentProvider)
 */
public class TextSearchTableContentProvider implements IStructuredContentProvider, ISearchResultListener {
	protected final Object[] EMPTY_ARRAY= new Object[0];
	private AbstractTextSearchResult fSearchResult;
	private TableViewer fTableViewer;

	/*
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof AbstractTextSearchResult)
			return ((AbstractTextSearchResult) inputElement).getElements();
		return EMPTY_ARRAY;
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		//nothing
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fTableViewer= (TableViewer) viewer;
		if(fSearchResult!=null){
		    fSearchResult.removeListener(this);
		}
		fSearchResult= (AbstractTextSearchResult) newInput;
		if(fSearchResult!=null){
		    fSearchResult.addListener(this);
		}
	}

	public void elementsChanged(Object[] updatedElements) {
		//TODO: copied from JavaSearchTableContentProvider
		int addCount= 0;
		int removeCount= 0;
		for (int i= 0; i < updatedElements.length; i++) {
			if (fSearchResult.getMatchCount(updatedElements[i]) > 0) {
				if (fTableViewer.testFindItem(updatedElements[i]) != null)
					fTableViewer.refresh(updatedElements[i]);
				else
					fTableViewer.add(updatedElements[i]);
				addCount++;
			} else {
				fTableViewer.remove(updatedElements[i]);
				removeCount++;
			}
		}
	}
	
	public void clear() {
		//TODO: copied from JavaSearchTableContentProvider
		fTableViewer.refresh();
	}

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultListener#searchResultChanged(org.eclipse.search.ui.SearchResultEvent)
     */
    public void searchResultChanged(SearchResultEvent e) {        
         fTableViewer.getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {            
                fTableViewer.refresh();
            }
        });
    }
}
