package org.csstudio.nams.configurator.editor.stackparts;

import org.csstudio.ams.configurationStoreService.knownTObjects.TopicTObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TopicStackPart extends AbstractStackPart {
	
	private Text _idTextEntry;
	private Text _nameTextEntry;
	private Composite _main;
	
	public TopicStackPart(Composite parent) {
		super(TopicTObject.class, 2);
		_main = new Composite(parent, SWT.NONE);
		_main.setLayout(new GridLayout(NUM_COLUMNS,false));
		_idTextEntry = this.createTextEntry(_main, "ID", false);
		_idTextEntry.setText("Topic");
		this.addSeparator(_main);
		_nameTextEntry = this.createTextEntry(_main, "Name", true);
	}

	@Override
	public Control getMainControl() {
		return _main;
	}
	
}
