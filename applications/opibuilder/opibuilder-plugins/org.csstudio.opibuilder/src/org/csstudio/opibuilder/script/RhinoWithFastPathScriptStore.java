/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.scriptUtil.PVUtil;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.simplepv.IPV;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * This is the implementation of {@link AbstractScriptStore} for Rhino script engine, with
 * special-case handlers for commonly used rule expressions. If possible, this script engine
 * implementation will avoid calling into javascript for rules where all expressions are on
 * the "fast path". This significantly improves CPU & memory performance for these rules.
 */
public class RhinoWithFastPathScriptStore extends AbstractScriptStore{

    private Context scriptContext;

    private Scriptable scriptScope;

    private Script script = null;
    
    private final boolean usesFastPath;
    private final RuleScriptData ruleScriptData;
    private String scriptString;
    private final Object initialRulePropertyValue;
    
    /**
     * Interface for fast-path handlers.
     */
    @FunctionalInterface
    public interface FastPathHandler {
    	public boolean handle(IPV[] pvList);
    }
    
    /**
     * Handler for expressions like pv0 == 0
     */
    public static final FastPathHandler PV0_EQ_0 = pvArr -> PVUtil.getDouble(pvArr[0]) == 0.0;
    
    /**
     * Handler for expressions like pv0 == 1
     */
    public static final FastPathHandler PV0_EQ_1 = pvArr -> PVUtil.getDouble(pvArr[0]) == 1.0;
    
    /**
     * Handler for expressions like pv0 != 0
     */
    public static final FastPathHandler PV0_NEQ_0 = pvArr -> PVUtil.getDouble(pvArr[0]) != 0.0;
    
    /**
     * Handler for expressions like pvInt0 == 0
     */
    public static final FastPathHandler PVINT0_EQ_0 = pvArr -> PVUtil.getLong(pvArr[0]) == 0;
    
    /**
     * Handler for expressions like pvInt0 == 1
     */
    public static final FastPathHandler PVINT0_EQ_1 = pvArr -> PVUtil.getLong(pvArr[0]) == 1; 
    
    /**
     * Handler for expressions like pvInt0 != 0
     */
    public static final FastPathHandler PVINT0_NEQ_0 = pvArr -> PVUtil.getLong(pvArr[0]) != 0;
    
    /**
     * Handler for expressions like true, 1, or 1 == 1
     */
    public static final FastPathHandler ALWAYS_TRUE = pvArr -> true;
    
    /**
     * Handler for expressions like false or 0
     */
    public static final FastPathHandler ALWAYS_FALSE = pvArr -> false;

    private static final Map<String, FastPathHandler> FAST_PATH_EXPRESSIONS = new HashMap<>();
    private static boolean logScriptsUsingJS = false;
    
    static {
    	FAST_PATH_EXPRESSIONS.put("pv0==0", PV0_EQ_0);
    	FAST_PATH_EXPRESSIONS.put("pv0 == 0", PV0_EQ_0);
    	
    	FAST_PATH_EXPRESSIONS.put("pv0==1", PV0_EQ_1);
    	FAST_PATH_EXPRESSIONS.put("pv0 == 1", PV0_EQ_1);
    	
    	FAST_PATH_EXPRESSIONS.put("pvInt0==0", PVINT0_EQ_0);
    	FAST_PATH_EXPRESSIONS.put("pvInt0 == 0", PVINT0_EQ_0);
    	
    	FAST_PATH_EXPRESSIONS.put("pvInt0==1", PVINT0_EQ_1);
    	FAST_PATH_EXPRESSIONS.put("pvInt0 == 1", PVINT0_EQ_1);
    	
    	FAST_PATH_EXPRESSIONS.put("pv0!=0", PV0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("pv0 != 0", PV0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("!(pv0==0)", PV0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("!(pv0 == 0)", PV0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("pv0", PV0_NEQ_0);
    	
    	FAST_PATH_EXPRESSIONS.put("pvInt0!=0", PVINT0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("pvInt0 != 0", PVINT0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("!(pvInt0==0)", PVINT0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("!(pvInt0 == 0)", PVINT0_NEQ_0);
    	FAST_PATH_EXPRESSIONS.put("pvInt0", PVINT0_NEQ_0);
    	
    	FAST_PATH_EXPRESSIONS.put("true", ALWAYS_TRUE);
    	FAST_PATH_EXPRESSIONS.put("1", ALWAYS_TRUE);
    	FAST_PATH_EXPRESSIONS.put("1==1", ALWAYS_TRUE);
    	FAST_PATH_EXPRESSIONS.put("1 == 1", ALWAYS_TRUE);
    	FAST_PATH_EXPRESSIONS.put("1===1", ALWAYS_TRUE);
    	FAST_PATH_EXPRESSIONS.put("1 === 1", ALWAYS_TRUE);
    	
    	FAST_PATH_EXPRESSIONS.put("false", ALWAYS_FALSE);
    	FAST_PATH_EXPRESSIONS.put("0", ALWAYS_FALSE);
    }
    
    /**
     * Adds a fast-path handler for the given string expression. The string must be an exact
     * match for the expression used in a rule - no parsing is performed (as it is an arbitrary 
     * JS expression, any parsing would need to be equivalent to writing a JS parser - this is hard).
     * 
     * @param expression the expression to match
     * @param handler a java handler which implements this expression
     */
    public static void addFastPathHandler(String expression, FastPathHandler handler) {
    	FAST_PATH_EXPRESSIONS.put(expression, handler);
    }

    /**
     * Initializes this script store.
     * @param scriptData the script data
     * @param editpart the part to which this script is connected
     * @param pvArray the pv array
     * @throws Exception on error
     */
    public RhinoWithFastPathScriptStore(final ScriptData scriptData, final AbstractBaseEditPart editpart,
            final IPV[] pvArray) throws Exception {
        super(scriptData, editpart, pvArray);
        usesFastPath = canUseFastPath(scriptData);
        if (usesFastPath) {
        	ruleScriptData = (RuleScriptData) scriptData;
        	initialRulePropertyValue = editpart.getWidgetModel().getProperty(ruleScriptData.getRuleData().getPropId()).getPropertyValue();
        } else {
        	ruleScriptData = null;
        	initialRulePropertyValue = null;
        }
    }

    /**
     * Don't actually do init here as it is called from superclass constructor before
     * we're able to figure out if we need to use fast or slow path.
     * 
     * Instead we lazily init only when needed.
     */
    @Override
    protected void initScriptEngine() throws Exception {
    }
    
    /**
     * Set whether to log scripts using the slow path.
     * @param log true to log
     */
    public static void setLogScriptsUsingJS(boolean log) {
    	logScriptsUsingJS = log;
    }
    
    /**
     * Lazily init, if needed. If initIfNeeded has already been called, do nothing.
     * @throws Exception on failure
     */
    private void initIfNeeded() throws Exception {
    	if (scriptScope != null) {
    		return;
    	}
    	
    	scriptContext = ScriptStoreFactory.getRhinoContext();
        scriptScope = new ImporterTopLevel(scriptContext);
        Object widgetController = Context.javaToJS(getEditPart(), scriptScope);
        Object pvArrayObject = Context.javaToJS(getPvArray(), scriptScope);
        Object displayObject = Context.javaToJS(getDisplayEditPart(), scriptScope);

        ScriptableObject.putProperty(scriptScope, ScriptService.WIDGET, widgetController);
        ScriptableObject.putProperty(scriptScope, ScriptService.PVS, pvArrayObject);
        ScriptableObject.putProperty(scriptScope, ScriptService.DISPLAY, displayObject);
        ScriptableObject.putProperty(scriptScope,
                ScriptService.WIDGET_CONTROLLER_DEPRECIATED, widgetController);
        ScriptableObject.putProperty(scriptScope,
                ScriptService.PV_ARRAY_DEPRECIATED, pvArrayObject);
    	
    }

    /**
     * Store the provided script, to be compiled lazily later if needed.
     * 
     * Note: this is called from superclass constructor so this.usesFastPath not available yet.
     */
    @Override
    protected void compileString(String string) throws Exception{
        scriptString = string;
    }

    /**
     * Store the provided script, to be compiled lazily later if needed.
     * 
     * Note: this is called from superclass constructor so this.usesFastPath not available yet.
     */
    @Override
    protected void compileInputStream(File file, InputStream s) throws Exception {
    	try (var isr = new InputStreamReader(s)) {
    		try (var br = new BufferedReader(isr)) {
    	        scriptString = br.lines().collect(Collectors.joining("\n"));
    		}
    	} finally {
    	    s.close();
    	}
    }

    /**
     * Execute the script, using the fast path if possible or falling back to JS if not.
     */
    @Override
    protected void execScript(final IPV triggerPV) throws Exception {
    	if (usesFastPath) {
    	    execFast();
    	} else {
    		execSlow(triggerPV);
    	}
    }
    
    /**
     * Execute the script using the JS implementation.
     * 
     * This will lazily compile the script the first time it is executed.
     */
    private void execSlow(final IPV triggerPV) throws Exception {
    	initIfNeeded();
        if (script == null) {
	        if (scriptString == null) {
	        	throw new IllegalStateException("script string was never set before execScript()");
	        }
	        
	        if (logScriptsUsingJS) {
	            OPIBuilderPlugin.getLogger().log(Level.INFO, String.format(
	            		"OPI script/rule will execute via javascript interpreter; likely to be slow. Content:\n%s\n", 
	            		scriptString));
	        }
            
    	script = scriptContext.compileString(scriptString, "script", 1, null);
        }
        ScriptableObject.putProperty(scriptScope,
                ScriptService.TRIGGER_PV, Context.javaToJS(triggerPV, scriptScope));
        script.exec(scriptContext, scriptScope);
    }
    
    /**
     * Decide whether the provided scriptData can use a fast-path implementation, or
     * whether it needs to fall back to the JS implementation.
     * 
     * @param scriptData the script data
     * @return true if can use fast path
     */
    private boolean canUseFastPath(final ScriptData scriptData) {
    	if (scriptData instanceof RuleScriptData) {
    		var ruleData = ((RuleScriptData) scriptData).getRuleData();
    		var rulesCanBeFast = ruleData.getExpressionList()
    				.stream()
    				.map(Expression::getBooleanExpression)
    				.allMatch(FAST_PATH_EXPRESSIONS::containsKey);
    		
    		if (ruleData.isOutputExpValue()) {
    			// If we are outputting expression, not only do the rules all have to be
    			// "fast", but the output expressions also all need to be fast.
    			// Currently this is limited to boolean-type output expressions
				final boolean isBooleanProperty = ruleData.getProperty() instanceof BooleanProperty;
    			return rulesCanBeFast && isBooleanProperty && ruleData.getExpressionList()
    					.stream()
    					.map(Expression::getValue)
    					.allMatch(FAST_PATH_EXPRESSIONS::containsKey);
    		} else {
    		    return rulesCanBeFast;
    		}
    	}
    	return false;
    }
    
    /**
     * Implementation of execScript for the fast-path case. This evaluates the expressions
     * using the fast-path handlers set up earlier, and the first one which returns true
     * gets it's property set on the widget.
     * 
     * If no handler matches, the initial property is set on the widget.
     * 
     * This should be logically equivalent to the script generated by RuleData.generateScript()
     * 
     * @throws Exception on failure
     */
    private void execFast() throws Exception {
    	final IPV[] pvArray = getPvArray();
    	
    	final var ruleData = ruleScriptData.getRuleData();
    	final var widgetModel = ruleData.getWidgetModel();
    	
    	for (final Expression e : ruleData.getExpressionList()) {
    		if (evaluateExpression(e.getBooleanExpression(), pvArray)) {
    			Object value;
    			if (ruleData.isOutputExpValue()) {
    				value = evaluateExpression(e.getValue(), pvArray);
    			} else {
    			    value = e.getValue();
    			}
    			widgetModel.setPropertyValue(ruleData.getPropId(), value);
    			return;
    		}
    	}
    	widgetModel.setPropertyValue(ruleData.getPropId(), initialRulePropertyValue);
    }
    
    /**
     * Evaluate an expression using it's fast-path handler.
     * 
     * @param e the expression
     * @param pvArray the pv array
     * @return the result of the evaluation
     */
    private boolean evaluateExpression(Object e, IPV[] pvArray) {
    	return FAST_PATH_EXPRESSIONS.get(e).handle(pvArray);
    }

}
