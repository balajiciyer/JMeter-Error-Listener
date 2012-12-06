/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package mindtree.jmeterplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.collections.Data;

/**
 * Create a ErrorListener test element, which saves the error and JMeter state information in set
 * of files
 *
 */
public class ErrorListenerGui extends AbstractListenerGui implements Serializable,ActionListener{

	private static final long serialVersionUID = 240L;

	/** Text Fields to hold the base properties*/
	private JTextField errorfilename;
	private JTextField propfilename;
	private JTextField variableName;
	private JTextField fileext;
	private JTextField basepath;

	/**Buttons providing options whether to log only properties or both errors and properties */
	private JRadioButton logOnlyProperties;
	private JRadioButton logErrorsAndProperties;
	private ButtonGroup group;

	/**Holds the property headers to be logeed to file*/
	static String[]propertyHeaderArray=new String[1000];

	/** JMeter Properties used intermediately*/
	JMeterProperty ErrorFile=null;
	JMeterProperty PropFile=null;
	JMeterProperty Variable=null;
	JMeterProperty FileExtension=null;
	JMeterProperty BasePath=null;
	JMeterProperty LogProperties=null;
	JMeterProperty LogErrors=null;
	JMeterProperty LogErrorsAndProperties=null;
	JMeterProperty LogOnlyProperties=null;
	static JMeterProperty JPropertyName=null;

	TestElement intermediateErrorListenerTestElement;

	/** Declarations for Properties Table*/

	JTextField JPropertyNameField;

	/** The table of configuration parameters. */
	private JTable table;

	/** The model for the parameter table. */
	private PowerTableModel tableModel;

	/** A button for adding new parameters to the table. */
	private JButton add;

	/** A button for removing parameters from the table. */
	private JButton delete;

	/** Command for adding a row to the table. */
	private static final String ADD = "add";

	/** Command for removing a row from the table. */
	private static final String DELETE = "delete";

	/**
	 * Boolean indicating whether or not this component should display its name.
	 * If true, this is a standalone component. If false, this component is
	 * intended to be used as a subpanel for another component.
	 */
	

	/** The resource names of the columns in the table. */
	private String COLUMN_NAMES_0 = "Properties";// $NON-NLS-1$

	public ErrorListenerGui() {
		super();
		
		init();  
	}

	/**
	 * Create a new SimpleConfigGui as either a standalone or an embedded
	 * component.
	 *
	 * @param displayName
	 *            indicates whether or not this component should display its
	 *            name. If true, this is a standalone component. If false, this
	 *            component is intended to be used as a subpanel for another
	 *            component.
	 */
	public ErrorListenerGui(boolean displayName) {
		
		init();
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
	public String getLabelResource() {
		return this.getClass().getSimpleName(); //$NON-NLS-1$
	}

	@Override
	public String getStaticLabel() {
		// TODO Auto-generated method stub

		return "Error Listener";

	}
	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
	@Override
	public void configure(TestElement el) {
		if(ErrorListener.updateAllowed==true)
		{
			initTableModel();
			table.setModel(tableModel);
			super.configure(el);
			ErrorFile=el.getProperty("ErrorFilePrefix");
			errorfilename.setText(ErrorFile.toString());

			PropFile=el.getProperty("PropertyFilePrefix");
			propfilename.setText(PropFile.toString());

			Variable=el.getProperty("VariableName");
			variableName.setText(Variable.toString());

			FileExtension=el.getProperty("FileExtensionName");
			fileext.setText(FileExtension.toString());

			BasePath=el.getProperty("FileBasePath");
			basepath.setText(BasePath.toString());

			LogOnlyProperties=el.getProperty("OnlyProperties");
			logOnlyProperties.setSelected(LogOnlyProperties.getBooleanValue());

			LogErrorsAndProperties=el.getProperty("ErrorsAndProperties");
			logErrorsAndProperties.setSelected(LogErrorsAndProperties.getBooleanValue());

			if (el instanceof ErrorListener) {
				tableModel.clearData();
				PropertyIterator iter = el.propertyIterator();
				while (iter.hasNext()) {
					JMeterProperty prop = iter.next();
					if(prop.getName().startsWith("JmeterProperty"))
						tableModel.addRow(new Object[]{prop.getStringValue()});
				}
			}
			checkDeleteStatus();
		}
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		ErrorListener errorMon = new ErrorListener();
		modifyTestElement(errorMon);
		return errorMon;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 *
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement errorListener) {
		if(ErrorListener.updateAllowed==true)
		{
			if (table.isEditing()) {
				table.getCellEditor().stopCellEditing();
			}

			intermediateErrorListenerTestElement=(TestElement)errorListener.clone();
			Data model = tableModel.getData();
			errorListener.clear();
			model.reset();
			Arrays.fill(propertyHeaderArray, null);
			int counter=0;

			while (model.next()) {
				errorListener.setProperty("JmeterProperty"+counter,(String)model.getColumnValue(COLUMN_NAMES_0).toString().trim());
				JPropertyName=errorListener.getProperty("JmeterProperty"+counter);
				propertyHeaderArray[counter]=JPropertyName.toString();
				counter++;
			};

			super.configureTestElement(errorListener);  

			//---ErrorResponse Field 
			errorListener.setProperty("ErrorFilePrefix",errorfilename.getText().trim());
			ErrorFile=errorListener.getProperty("ErrorFilePrefix");
			ErrorListener.ERRORFILE=ErrorFile.toString();

			//---Properties Field
			errorListener.setProperty("PropertyFilePrefix",propfilename.getText().trim());
			PropFile=errorListener.getProperty("PropertyFilePrefix");
			ErrorListener.PROPFILE=PropFile.toString();
			//---

			//---Variable Field
			AbstractTestElement at = (AbstractTestElement) errorListener;
			at.setProperty("VariableName",variableName.getText().trim());
			Variable=errorListener.getProperty("VariableName");
			ErrorListener.VARIABLE_NAME=Variable.toString();

			//---File Extension

			errorListener.setProperty("FileExtensionName",fileext.getText().trim());
			FileExtension=errorListener.getProperty("FileExtensionName");
			ErrorListener.FILEEXT=FileExtension.toString();

			//---Base Path
			errorListener.setProperty("FileBasePath",basepath.getText().trim());
			BasePath=errorListener.getProperty("FileBasePath");
			ErrorListener.BASEPATH=BasePath.toString();

			//---Only Properties
			errorListener.setProperty("OnlyProperties",logOnlyProperties.isSelected());
			LogOnlyProperties=errorListener.getProperty("OnlyProperties");
			ErrorListener.ONLYPROPS=LogOnlyProperties.getBooleanValue();

			//---Errors and Properties
			errorListener.setProperty("ErrorsAndProperties",logErrorsAndProperties.isSelected());
			LogErrorsAndProperties=errorListener.getProperty("ErrorsAndProperties");
			ErrorListener.ERRORSANDPROPS=LogErrorsAndProperties.getBooleanValue();
		}
	}

	/**
	 * Implements JMeterGUIComponent.clearGui
	 */
	@Override
	public void clearGui() {
		if(ErrorListener.updateAllowed==true)
		{
			super.clearGui();
			initTableModel();
			table.setModel(tableModel);
			errorfilename.setText(""); //$NON-NLS-1$
			propfilename.setText(""); //$NON-NLS-1$
			variableName.setText(""); //$NON-NLS-1$
			fileext.setText("");
			basepath.setText("");//$NON-NLS-1$
			logErrorsAndProperties.setSelected(true);
			Arrays.fill(propertyHeaderArray, null);
		}
	}

	/**
	 * Initialize the components and layout of this component.
	 */
	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		Box box = Box.createVerticalBox();	        
		box.add(makeTitlePanel());
		box.add(createBasePathPanel());
		box.add(createErrorFilenamePrefixPanel());
		box.add(createPropFilenamePrefixPanel());
		box.add(createVariableNamePanel());
		box.add(createFileExtPanel());
		add(createTablePanel(), BorderLayout.CENTER);
		//Force the table to be at least 70 pixels high
		add(Box.createVerticalStrut(70), BorderLayout.WEST);
		add(createButtonPanel(), BorderLayout.SOUTH);
		add(box, BorderLayout.NORTH);
	}

	/**
	 * Invoked when an action occurs. This implementation supports the add and
	 * delete buttons.
	 *
	 * @param e
	 *            the event that has occurred
	 */
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals(DELETE)) {
			deleteArgument();
		} else if (action.equals(ADD)) {
			addArgument();
		}
	}

	/**
	 * Create a GUI panel containing the table of configuration parameters.
	 *
	 * @return a GUI panel containing the parameter table
	 */
	private Component createTablePanel() {
		tableModel = new PowerTableModel(
				new String[] { COLUMN_NAMES_0 },
				new Class[] { String.class});

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return makeScrollPane(table);
	}

	protected void initTableModel() {
		tableModel = new PowerTableModel(
				new String[] { COLUMN_NAMES_0 },
				new Class[] { String.class});
	}

	/**
	 * Create a panel containing the add and delete buttons.
	 *
	 * @return a GUI panel containing the buttons
	 */
	private JPanel createButtonPanel() {
		add = new JButton(JMeterUtils.getResString("add")); //$NON-NLS-1$
		add.setActionCommand(ADD);
		add.addActionListener(this);
		add.setEnabled(true);

		delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
		delete.setActionCommand(DELETE);
		delete.addActionListener(this);

		checkDeleteStatus();

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(add);
		buttonPanel.add(delete);
		return buttonPanel;
	}

	/**
	 * Add a new argument row to the table.
	 */
	protected void addArgument() {
		//Give warning to user if the no of properties exceeds the default value i.e. 6
		checkRowCountStatus();

		// If a table cell is being edited, we should accept the current value
		// and stop the editing before adding a new row.
		stopTableEditing();
		tableModel.addNewRow();
		tableModel.fireTableDataChanged();

		// Enable DELETE (which may already be enabled, but it won't hurt)
		delete.setEnabled(true);

		// Highlight (select) the appropriate row.
		int rowToSelect = tableModel.getRowCount() - 1;
		table.setRowSelectionInterval(rowToSelect, rowToSelect);

	}

	/**
	 * Stop any editing that is currently being done on the table. This will
	 * save any changes that have already been made.
	 */
	protected void stopTableEditing() {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.stopCellEditing();
		}
	}

	/**
	 * Remove the currently selected argument from the table.
	 */
	protected void deleteArgument() {
		// If a table cell is being edited, we must cancel the editing before
		// deleting the row
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}

		int rowSelected = table.getSelectedRow();

		if (rowSelected >= 0) {

			/**Delete the property value of selected row from the context*/
			JMeterProperty SelectedRowValue = null;
			String rowSelectedValue = tableModel.getValueAt(table.getSelectedRow(),0).toString();
			intermediateErrorListenerTestElement.setProperty("SelectedRowValue", rowSelectedValue);
			SelectedRowValue=intermediateErrorListenerTestElement.getProperty("SelectedRowValue");
			intermediateErrorListenerTestElement.removeProperty(SelectedRowValue.toString());
			intermediateErrorListenerTestElement.setProperty("SelectedRowValue",null);

			// removeProperty(tableModel.getValueAt (
			// table.getSelectedRow(),0).toString());
			tableModel.removeRow(rowSelected);
			tableModel.fireTableDataChanged();

			// Disable DELETE if there are no rows in the table to delete.
			if (tableModel.getRowCount() == 0) {
				delete.setEnabled(false);
			} else {
				// Table still contains one or more rows, so highlight (select)
				// the appropriate one.
				int rowToSelect = rowSelected;

				if (rowSelected >= tableModel.getRowCount()) {
					rowToSelect = rowSelected - 1;
				}

				table.setRowSelectionInterval(rowToSelect, rowToSelect);
			}
		}
		else
		{
			return;
		}
	}
	
	/**
	 * Create a panel containing base directory path
	 */
	public JPanel createBasePathPanel()
	{
		JLabel label = new JLabel("Base Directory:"); // $NON-NLS-1$

		basepath = new JTextField(10);
		basepath.setName(ErrorListener.BASEPATH);
		label.setLabelFor(basepath);

		JPanel basepathPanel = new JPanel(new BorderLayout(72, 0));
		basepathPanel.add(label, BorderLayout.WEST);
		basepathPanel.add(basepath, BorderLayout.CENTER);
		return basepathPanel;
	}
	/**
	 * Create a panel containing error filename prefix
	 */
	public JPanel createErrorFilenamePrefixPanel()
	{
		JLabel label = new JLabel("Error Filename Prefix:"); // $NON-NLS-1$

		errorfilename = new JTextField(10);
		errorfilename.setName(ErrorListener.ERRORFILE);
		label.setLabelFor(errorfilename);

		JPanel errorfilenamePanel = new JPanel(new BorderLayout(36, 0));
		errorfilenamePanel.setPreferredSize(new Dimension(24,24));
		errorfilenamePanel.add(label, BorderLayout.WEST);
		errorfilenamePanel.add(errorfilename, BorderLayout.CENTER);
		return errorfilenamePanel;
	}
	/**
	 * Create a panel containing property filename prefix
	 */
	public JPanel createPropFilenamePrefixPanel()
	{
		JLabel label = new JLabel("Properties Filename Prefix:"); // $NON-NLS-1$

		propfilename = new JTextField(10);
		propfilename.setName(ErrorListener.PROPFILE);
		label.setLabelFor(propfilename);

		JPanel propfilenamePanel = new JPanel(new BorderLayout(5, 0));
		propfilenamePanel.add(label, BorderLayout.WEST);
		propfilenamePanel.add(propfilename, BorderLayout.CENTER);
		return propfilenamePanel;

	}
	/**
	 * Create a panel containing Variable name which holds the error file name
	 */
	public JPanel createVariableNamePanel()
	{
		JLabel label = new JLabel("Variable Name:"); // $NON-NLS-1$

		variableName = new JTextField(10);
		variableName.setName(ErrorListener.VARIABLE_NAME);
		label.setLabelFor(variableName);

		JPanel variablenamePanel = new JPanel(new BorderLayout(73, 0));
		variablenamePanel.add(label, BorderLayout.WEST);
		variablenamePanel.add(variableName, BorderLayout.CENTER);
		return variablenamePanel;
	}
	/**
	 * Create a panel containing error filename extension
	 */
	public JPanel createFileExtPanel()
	{
		JLabel label = new JLabel("Response File Extension:"); // $NON-NLS-1$

		fileext = new JTextField(10);
		fileext.setName(ErrorListener.FILEEXT);
		label.setLabelFor(fileext);

		JPanel fileextPanel = new JPanel(new BorderLayout(18, 0));
		fileextPanel.add(label, BorderLayout.WEST);
		fileextPanel.add(fileext, BorderLayout.CENTER);

		fileextPanel.add(Box.createHorizontalStrut(label.getPreferredSize().width
				+ fileext.getPreferredSize().width), BorderLayout.NORTH);

		//Only Properties radio button
		JPanel optionPanel = new JPanel();
		group = new ButtonGroup();
		logOnlyProperties = new JRadioButton("Log Only Properties"); // $NON-NLS-1$
		group.add(logOnlyProperties);
		optionPanel.add(logOnlyProperties);

		//Errors and Properties radio button
		logErrorsAndProperties = new JRadioButton("Log Errors and Properties"); // $NON-NLS-1$
		group.add(logErrorsAndProperties);
		optionPanel.add(logErrorsAndProperties);
		fileextPanel.add(optionPanel,BorderLayout.SOUTH);

		logErrorsAndProperties.setSelected(true);
		logErrorsAndProperties.setActionCommand(ErrorListener.LOG_PROPERTIES);
		logErrorsAndProperties.setActionCommand(ErrorListener.LOG_ERRORS_PROPERTIES);	      
		return fileextPanel;
	}

	/**
	 * Enable or disable the delete button depending on whether or not there is
	 * a row to be deleted.
	 */
	protected void checkDeleteStatus() {
		// Disable DELETE if there are no rows in the table to delete.
		if (tableModel.getRowCount() == 0) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
		}
	}
	/** Not used currently*/
	protected boolean checkAddStatus() {
		// Disable ADD if number of rows exceeds the Jproperty count in the table
		if (tableModel.getRowCount()==Integer.parseInt(ErrorListener.JPropCount)) {
			JOptionPane.showMessageDialog(null, "Nice Try!!!But you cannot add more properties than configured in the properties file"
					, "Information",
					JOptionPane.OK_OPTION);
			add.setEnabled(false);
			return true;
		} 
		else {
			add.setEnabled(true);
			return false;
		}
	}

	/**
	 * Check the number of properties defined by the user 
	 * and display warning if it exceeds 6
	 */
	protected void checkRowCountStatus() {
		// Give warning to user if the property count exceeds 6 
		if (tableModel.getRowCount()==6) {
			JOptionPane.showMessageDialog(null, 
					"Increasing the number of properties shall impact the performance",
					"Warning",
					JOptionPane.WARNING_MESSAGE);	           	            
		} 
	}
	//Needed to avoid Class cast error in Clear.java
	public void clearData() {
	}

}
