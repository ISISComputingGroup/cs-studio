package org.csstudio.nams.configurator.treeviewer;

import org.csstudio.nams.configurator.treeviewer.actions.NewEntryAction;
import org.csstudio.nams.configurator.treeviewer.treecomponents.AbstractNode;
import org.csstudio.nams.configurator.treeviewer.treecomponents.Categories;
import org.csstudio.nams.configurator.treeviewer.treecomponents.CategoryNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

public class ConfigurationTreeView extends ViewPart {
	
	public static final String ID = "org.csstudio.nams.configurator.views.ConfigurationTreeView";
	
	private TreeViewer _viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action _newAction;
	private Action doubleClickAction;
	private AbstractNode _root;

	/**
	 * The constructor.
	 */
	public ConfigurationTreeView() {
		_root = this.createTreeModel();
	}

	private AbstractNode createTreeModel() {
		AbstractNode root = new CategoryNode(Categories.ROOT);
		root.addChild(new CategoryNode(Categories.USER));
		root.addChild(new CategoryNode(Categories.TOPIC));
		root.addChild(new CategoryNode(Categories.USERGROUP));
		root.addChild(new CategoryNode(Categories.FILTERCONDITION));
		root.addChild(new CategoryNode(Categories.FILTER));
		return root;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(_viewer);
		_viewer.setContentProvider(new ConfigurationContentProvider());
		_viewer.setLabelProvider(new ConfigurationLabelProvider());
		_viewer.setSorter(new ConfigurationSorter());
		_viewer.setInput(_root);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		this.getSite().setSelectionProvider(_viewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ConfigurationTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(_viewer.getControl());
		_viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, _viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(_newAction);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		if (!_viewer.getSelection().isEmpty()) {
			manager.add(_newAction);
		}
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(_newAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		_newAction = new NewEntryAction(_viewer);
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = _viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		_viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			_viewer.getControl().getShell(),
			"Configuration Tree Viewer",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		_viewer.getControl().setFocus();
	}
}