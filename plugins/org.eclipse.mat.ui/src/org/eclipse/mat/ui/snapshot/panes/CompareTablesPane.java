/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.ui.snapshot.panes;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.registry.QueryResult;
import org.eclipse.mat.ui.MemoryAnalyserPlugin;
import org.eclipse.mat.ui.compare.CompareTablesQuery.ComparedColumn;
import org.eclipse.mat.ui.compare.CompareTablesQuery.Mode;
import org.eclipse.mat.ui.compare.CompareTablesQuery.TableComparisonResult;
import org.eclipse.mat.ui.internal.panes.QueryResultPane;
import org.eclipse.mat.ui.internal.viewer.RefinedResultViewer;
import org.eclipse.mat.ui.util.EasyToolBarDropDown;
import org.eclipse.mat.ui.util.PopupMenu;
import org.eclipse.ui.PlatformUI;

public class CompareTablesPane extends QueryResultPane
{

	public enum DiffOption
	{
		ABSOLUTE("Absolute Values"), DIFF_TO_BASE("Difference to Base Table"), DIFF_TO_PREV("Difference to Preceding Table");

		String label;

		private DiffOption(String label)
		{
			this.label = label;
		}

		public String toString()
		{
			return label;
		}
	}

	private DiffOption diffOption;

	@Override
	public void initWithArgument(Object argument)
	{
		super.initWithArgument(argument);

		IResult subject = ((QueryResult) argument).getSubject();
		if (subject instanceof TableComparisonResult)
		{
			Mode mode = ((TableComparisonResult) subject).getMode();
			switch (mode)
			{
			case ABSOLUTE:
				diffOption = DiffOption.ABSOLUTE;
				break;
			case DIFF_TO_FIRST:
				diffOption = DiffOption.DIFF_TO_BASE;
				break;
			case DIFF_TO_PREVIOUS:
				diffOption = DiffOption.DIFF_TO_PREV;
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void contributeToToolBar(IToolBarManager manager)
	{
		addDiffOptions(manager);

		addSelectColumns(manager);

		manager.add(new Separator());
		super.contributeToToolBar(manager);

	}

	private void addSelectColumns(IToolBarManager manager)
	{
		final IResult result = viewer.getQueryResult().getSubject();
		if (result instanceof TableComparisonResult)
		{
			Action selectColumnsAction = new EasyToolBarDropDown("Select displayed columns...", //
					MemoryAnalyserPlugin.getImageDescriptor(MemoryAnalyserPlugin.ISharedImages.SELECT_COLUMN), this) {

				@Override
				public void contribute(PopupMenu menu)
				{
					List<ComparedColumn> columns = ((TableComparisonResult) result).getComparedColumns();
					for (ComparedColumn comparedColumn : columns)
					{
						menu.add(new SelectColumnAction(comparedColumn));
					}

				}
			};
			
			manager.add(selectColumnsAction);
		}

	}

	private void addDiffOptions(IToolBarManager manager)
	{
		Action diffOptionAction = new EasyToolBarDropDown("Choose diff option ...", //
				MemoryAnalyserPlugin.getImageDescriptor(MemoryAnalyserPlugin.ISharedImages.GROUPING), this) {
			@Override
			public void contribute(PopupMenu menu)
			{
				for (DiffOption opt : DiffOption.values())
				{
					Action action = new DiffOptionAction(opt);
					action.setEnabled(opt != diffOption);
					action.setChecked(opt == diffOption);
					menu.add(action);
				}
			}
		};

		manager.add(diffOptionAction);
	}

	private class DiffOptionAction extends Action
	{
		private DiffOption diffOption;

		private DiffOptionAction(DiffOption diffOption)
		{
			super(diffOption.toString(), AS_CHECK_BOX);
			this.diffOption = diffOption;
		}

		@Override
		public void run()
		{
			// do not run the same action twice - selection was not changed
			if (!isChecked()) return;

			Mode mode = Mode.ABSOLUTE;
			switch (diffOption)
			{
			case DIFF_TO_PREV:
				mode = Mode.DIFF_TO_PREVIOUS;

				break;
			case DIFF_TO_BASE:
				mode = Mode.DIFF_TO_FIRST;

				break;

			default:
				break;
			}

			IResult result = viewer.getQueryResult().getSubject();
			if (result instanceof TableComparisonResult)
			{
				((TableComparisonResult) result).setMode(mode);
			}

			final QueryResult queryResult = new QueryResult(null, "compare", result);

			new Job(getText()) {
				protected IStatus run(IProgressMonitor monitor)
				{

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run()
						{
							CompareTablesPane.this.diffOption = diffOption;
							deactivateViewer();
							RefinedResultViewer v = createViewer(queryResult);
							activateViewer(v);
						}
					});
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private class SelectColumnAction extends Action
	{
		ComparedColumn column;

		public SelectColumnAction(ComparedColumn column)
		{
			super(column.getDescription().getLabel(), AS_CHECK_BOX);
			this.column = column;
			setChecked(column.isDisplayed());
		}

		@Override
		public void run()
		{
			column.setDisplayed(!column.isDisplayed());
			
			IResult result = viewer.getQueryResult().getSubject();
			if (result instanceof TableComparisonResult)
			{
				((TableComparisonResult) result).updateColumns();
			}

			final QueryResult queryResult = new QueryResult(null, "compare", result);

			new Job(getText()) {
				protected IStatus run(IProgressMonitor monitor)
				{

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run()
						{
							deactivateViewer();
							RefinedResultViewer v = createViewer(queryResult);
							activateViewer(v);
						}
					});
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

}