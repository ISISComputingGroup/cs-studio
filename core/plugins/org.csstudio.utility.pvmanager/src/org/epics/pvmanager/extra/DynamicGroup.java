/*
 * Copyright 2008-2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.extra;

import java.util.ArrayList;
import java.util.List;
import org.epics.pvmanager.DataRecipe;
import org.epics.pvmanager.DataSource;
import org.epics.pvmanager.DesiredRateExpression;
import org.epics.pvmanager.ExceptionHandler;
import org.epics.pvmanager.PVManager;

/**
 * A expression that returns the result of a dynamically managed group.
 * Once the group is created, any {@link DesiredRateExpression} can be
 * added dynamically. The exceptions eventually generated by those
 * expressions can be obtained through {@link #lastExceptions() }.
 *
 * @author carcassi
 */
public class DynamicGroup extends DesiredRateExpression<List<Object>> {

    private final DataSource dataSource = PVManager.getDefaultDataSource();
    private final List<DataRecipe> recipes = new ArrayList<DataRecipe>();

    /**
     * Creates a new group.
     */
    public DynamicGroup() {
        super((DesiredRateExpression<?>) null, new DynamicGroupFunction(), "dynamic group");
    }

    DynamicGroupFunction getGroup() {
        return (DynamicGroupFunction) getFunction();
    }

    /**
     * Returns the last exception for each expression in the group (if present).
     * 
     * @return a list of exceptions (never null)
     */
    public List<Exception> lastExceptions() {
        synchronized (getGroup()) {
            return new ArrayList<Exception>(getGroup().getExceptions());
        }
    }

    /**
     * Removes all the expressions currently in the group.
     * 
     * @return this
     */
    public synchronized DynamicGroup clear() {
        for (int index = recipes.size() - 1; index >= 0; index--) {
            DataRecipe recipe = recipes.remove(index);
            dataSource.disconnect(recipe);
            synchronized (getGroup()) {
                getGroup().getArguments().remove(index);
                getGroup().getExceptions().remove(index);
                getGroup().getPreviousValues().remove(index);
            }
        }
        return this;
    }

    /**
     * Returns the number of expressions in the group.
     * 
     * @return number of expressions in the group
     */
    public synchronized int size() {
        return recipes.size();
    }

    /**
     * Adds the expression at the end.
     * 
     * @param expression the expression to be added
     * @return this
     */
    public synchronized DynamicGroup add(DesiredRateExpression<?> expression) {
        DataRecipe recipe = expression.getDataRecipe();
        recipe = recipe.withExceptionHandler(handlerFor(recipes.size()));
        synchronized (getGroup()) {
            getGroup().getArguments().add(expression.getFunction());
            getGroup().getExceptions().add(null);
            getGroup().getPreviousValues().add(null);
        }
        dataSource.connect(recipe);
        recipes.add(recipe);
        return this;
    }

    /**
     * Removes the expression at the given location.
     * 
     * @param index the position to remove
     * @return this
     */
    public synchronized DynamicGroup remove(int index) {
        DataRecipe recipe = recipes.remove(index);
        dataSource.disconnect(recipe);
        synchronized (getGroup()) {
            getGroup().getArguments().remove(index);
            getGroup().getExceptions().remove(index);
            getGroup().getPreviousValues().remove(index);
        }
        return this;
    }

    /**
     * Changes the expression to the given location.
     * 
     * @param index the position to remove
     * @param expression the new expression
     * @return this
     */
    public synchronized DynamicGroup set(int index, DesiredRateExpression<?> expression) {
        DataRecipe recipe = expression.getDataRecipe();
        recipe = recipe.withExceptionHandler(handlerFor(index));
        DataRecipe oldRecipe = recipes.get(index);
        dataSource.disconnect(oldRecipe);

        synchronized (getGroup()) {
            getGroup().getArguments().set(index, expression.getFunction());
            getGroup().getExceptions().set(index, null);
            getGroup().getPreviousValues().set(index, null);
        }
        dataSource.connect(recipe);
        recipes.set(index, recipe);
        return this;
    }

    private ExceptionHandler handlerFor(final int index) {
        return new ExceptionHandler() {

            @Override
            public void handleException(Exception ex) {
                synchronized (getGroup()) {
                    getGroup().getExceptions().set(index, ex);
                }
            }
        };
    }
}
