/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others)
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

/*
 */
package org.cs3.prolog.ui.util;



import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Control;

/**
 */
public interface PropertyEditor {
    public Control getControl();
    public String getValue();
    public String getKey();
    public void setValue(String value);
    public String validate();
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    public void addPropertyChangeListener(IPropertyChangeListener l);
    public void removePropertyChangeListener(IPropertyChangeListener l);
    public void revertToDefault();
}

