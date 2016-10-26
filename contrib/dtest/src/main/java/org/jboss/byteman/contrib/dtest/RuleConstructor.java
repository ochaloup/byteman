/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2016,
 * @author JBoss, by Red Hat.
 */
package org.jboss.byteman.contrib.dtest;

/**
 * <p>
 * Provides a fluent API for creating Byteman rules without needing
 *  to mess around with String concatenation.
 * <p>
 * Example:
 * <p>
 * <code>
 * RuleBuilder rb = new RuleBuilder("myRule");<br>
 * rb.onClass("org.jboss.byteman.ExampleClass")<br>
 *   .inMethod("doInterestingStuff")<br>
 *   .whenTrue().doAction("myAction()");<br>
 *  System.out.println(rb);
 * </code>
 * <p>
 * will print:
 * <p>
 * <code>
 *   RULE myRule<br>
 *   CLASS org.jboss.byteman.ExampleClass<br>
 *   METHOD doInterestingStuffv
 *   AT ENTRY<br>
 *   IF true<br>
 *   DO myAction()<br>
 *   ENDRULE
 * </code>
 */
public final class RuleConstructor {
    private static final String LINEBREAK = String.format("%n");

    private static final String CONSTRUCTOR_METHOD = "<init>";
    private static final String CLASS_CONSTRUCTOR = "<clinit>";

    private MethodClause methodClause;
    private LocationClause locationClause;
    private ConditionClause conditionClause;
    private ActionClause actionClause;
    private Builder builderClause;

    private String ruleName;
    private String className;
    private boolean isInterface;
    private boolean isIncludeSubclases;
    private String methodName;
    private String helperName;
    private String where = "AT ENTRY";
    private String bind;
    private String when = "true";
    private String action;
    private String imports;
    private String compile;

    /**
     * Rule builder initialization.
     *
     * @param ruleName  name of rule is demanded information
     */
    public RuleConstructor(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * To get to specific point of builder flow api.
     * You will be able to set <code>onMethod</code> rule specifiers.
     */
    public MethodClause getMethodClause() {
        return methodClause;
    }

    /**
     * To get to specific point of builder flow api.
     * You will be able to set <code>at</code> rule specifiers.
     */
    public LocationClause getLocationClause() {
        return locationClause;
    }

    /**
     * To get to specific point of builder flow api.
     * You will be able to set <code>when</code> rule specifiers.
     */
    public ConditionClause getConditionClause() {
        return conditionClause;
    }

    /**
     * To get to specific point of builder flow api.
     * You will be able to set <code>action</code> rule specifiers.
     */
    public ActionClause getActionClause() {
        return actionClause;
    }

    /**
     * Generate the rule as a string.
     *
     * @return  rule in string form
     */
    public String build() {
        if(this.builderClause == null) {
            throw new IllegalStateException("Not all data for builder was specified. " +
                "Current rule shape is\n" + new Builder().build());
        }
        return this.builderClause.build();
    }

    /**
     * Class that rule event is associated to.
     *
     * @param clazz  class as target of rule injection
     * @return this, for having fluent api
     */
    public RuleConstructor.MethodClause onClass(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), false);
    }

    /**
     * Class name that rule event is associated to.
     *
     * @param className  class name as target of rule injection
     * @return this, for having fluent api
     */
    public RuleConstructor.MethodClause onClass(String className) {
        return onSpecifier(className, false);
    }

    /**
     * Interface class that rule event is associated to.
     *
     * @param clazz  interface class as target of rule injection
     * @return this, for having fluent api
     */
    public RuleConstructor.MethodClause onInterface(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), true);
    }

    /**
     * Interface class name that rule event is associated to.
     *
     * @param className interface class name as target of rule injection
     * @return this, for having fluent api
     */
    public RuleConstructor.MethodClause onInterface(String className) {
        return onSpecifier(className, true);
    }

    private RuleConstructor.MethodClause onSpecifier(String className, boolean isInterface) {
        this.className = className;
        this.isInterface = isInterface;
        if(this.methodClause == null) this.methodClause = this.new MethodClause();
        return this.methodClause;
    }

    public final class MethodClause {
        /**
         * Defining that the rule will be injected to all sub-classes
         * or classes implementing the interface.<br>
         * By default byteman injects the rule only to the specified
         * class and it left children classes untouched.<br>
         * The rule class definition is changed to <code>CLASS ^org.jboss.byteman.ExampleClass</code>.
         *
         * @return this, for having fluent api
         */
        public RuleConstructor.MethodClause includeSubclases() {
            RuleConstructor.this.isIncludeSubclases = true;
            return this;
        }

        /**
         * Defining method where the rule is injected to.
         *
         * @param methodName  method name for rule injection
         * @return this, for having fluent api
         */
        public RuleConstructor.LocationClause inMethod(String methodName) {
            RuleConstructor.this.methodName = methodName;
            if(RuleConstructor.this.locationClause == null) {
                RuleConstructor.this.locationClause = RuleConstructor.this.new LocationClause();
            }
            return RuleConstructor.this.locationClause;
        }

        /**
         * <p>
         * Defining method specified by argument types where the rule is injected to.
         * <p>
         * Example:
         * <p>
         * <code>
         *   new RuleBuilder("rule name")<br>
         *     .onInterface("javax.transaction.xa.XAResource")<br>
         *     .inMethod("commit", "Xid" , "boolean")<br>
         *     ...
         * </code>
         *
         * @param methodName  method name for rule injection
         * @param argTypes  method argument types to closer specify which method
         * @return  this, for having fluent api
         */
        public RuleConstructor.LocationClause inMethod(String methodName, String... argTypes) {
            RuleConstructor.this.methodName = methodName + "(" + stringJoin(",", argTypes) + ")";
            if(RuleConstructor.this.locationClause == null) {
                RuleConstructor.this.locationClause = RuleConstructor.this.new LocationClause();
            }
            return RuleConstructor.this.locationClause;
        }

        /**
         * Defining constructor, special method type,
         * as place for rule injection.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.LocationClause inConstructor() {
            return inMethod(CONSTRUCTOR_METHOD);
        }

        /**
         * Defining constructor, special method type,
         * as place for rule injection.<br>
         * The constructor is closer specified by its arguments.
         *
         * @param argTypes  method argument types to closer specify which method
         * @return  this, for having fluent api
         */
        public RuleConstructor.LocationClause inConstructor(String... argTypes) {
            return inMethod(CONSTRUCTOR_METHOD, argTypes);
        }

        /**
         * Defining class initialization method as place for rule injection.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.LocationClause inClassInitMethod() {
            return inMethod(CLASS_CONSTRUCTOR);
        }

        /**
         * Defining class initialization method as place for rule injection.
         *
         * @param argTypes  method argument types to closer specify which method
         * @return  this, for having fluent api
         */
        public RuleConstructor.LocationClause inClassInitMethod(String... argTypes) {
            return inMethod(CLASS_CONSTRUCTOR, argTypes);
        }

        /**
         * Returning this {@link RuleConstructor} - parent in the flow api. 
         */
        public RuleConstructor parent() {
            return RuleConstructor.this;
        }
    }

    public final class LocationClause {
        /**
         * Location specifier definition.<br>
         * Defined string is directly used in rule definition.
         *
         * @param where  location specifier
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause where(String where) {
            RuleConstructor.this.where = where;
            if(RuleConstructor.this.conditionClause == null) {
                RuleConstructor.this.conditionClause = RuleConstructor.this.new ConditionClause();
            }
            return RuleConstructor.this.conditionClause;
        }

        /**
         * Rule is invoked at entry point of method.<br>
         * Location specifier is set as <code>AT ENTRY</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atEntry() {
            return at("ENTRY");
        }

        /**
         * Rule is invoked at exit point of method.<br>
         * Location specifier is set as <code>AT EXIT</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atExit() {
            return at("EXIT");
        }

        /**
         * Rule is invoked at specific line of code
         * within the method.<br>
         * Location specifier is set as <code>AT LINE &lt;line&gt;</code>.
         *
         * @param line  line number to be rule injection point
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atLine(int line) {
            return at("LINE " + line);
        }

        /**
         * Rule is invoked at point where method reads a variable.<br>
         * Location specifier is set as <code>AT READ &lt;variable&gt;</code>.
         *
         * @param variable  rule is triggered at write from this variable happen
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atRead(String variable) {
            return at("READ " + variable);
        }

        /**
         * Rule is invoked at point where method reads a variable
         * where <code>occurencePosition</code> defines Nth textual occurrence of the field access.<br>
         * Location specifier is set as <code>AT READ &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param variable  rule is triggered at write from this variable happen
         * @param occurencePosition  Nth textual occurrence of reading the field
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atRead(String variable, int occurencePosition) {
            return at("READ " + variable + " " + occurencePosition);
        }

        /**
         * Rule is invoked after point where method reads a variable.<br>
         * Location specifier is set as <code>AFTER READ &lt;variable&gt;</code>.
         *
         * @param variable  rule is triggered after write from this variable happen
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterRead(String variable) {
            return after("READ " + variable);
        }

        /**
         * Rule is invoked after point where method reads a variable
         * where <code>occurencePosition</code> defines Nth textual occurrence of the field access.<br>
         * Location specifier is set as <code>AFTER READ &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param variable  rule is triggered after write from this variable happen
         * @param occurencePosition  Nth textual occurrence of reading the field
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterRead(String variable, int occurencePosition) {
            return after("READ " + variable + " " + occurencePosition);
        }

        /**
         * Rule is invoked at point where method writes to a variable.<br>
         * Location specifier is set as <code>AT WRITE &lt;variable&gt;</code>.
         *
         * @param variable  rule is triggered at write to this variable happen
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atWrite(String variable) {
            return at("WRITE " + variable);
        }

        /**
         * Rule is invoked at point where method writes to a variable
         * where <code>occurencePosition</code> defines Nth textual occurrence of the field write.<br>
         * Location specifier is set as <code>AT WRITE &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param variable  rule is triggered at write to this variable happen
         * @param occurencePosition  Nth textual occurrence of the field write
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atWrite(String variable, int occurencePosition) {
            return at("WRITE " + variable + " " + occurencePosition);
        }

        /**
         * Rule is invoked after point where method writes to a variable.<br>
         * Location specifier is set as <code>AFTER WRITE &lt;variable&gt;</code>.
         *
         * @param variable  rule is triggered after write to this variable happen
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterWrite(String variable) {
            return after("WRITE " + variable);
        }

        /**
         * Rule is invoked after point where method writes to a variable
         * where <code>occurencePosition</code> defines Nth textual occurrence of the field write.<br>
         * Location specifier is set as <code>AFTER WRITE &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param variable  rule is triggered after write to this variable happen
         * @param occurencePosition  Nth textual occurrence of the field write
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterWrite(String variable, int occurencePosition) {
            return after("WRITE " + variable + " " + occurencePosition);
        }

        /**
         * Rule is invoked at point of invocation of method within the trigger method.<br>
         * Location specifier is set as <code>AT INVOKE &lt;variable&gt;</code>.
         *
         * @param method  method name after which invocation the rule is executed
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atInvoke(String method) {
            return at("INVOKE " + method);
        }

        /**
         * Rule is invoked at point of invocation of method within the trigger method
         * where <code>occurencePosition</code> defines Nth textual occurrence of the method invocation.<br>
         * Location specifier is set as <code>AT INVOKE &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param method  method name after which invocation the rule is executed
         * @param occurencePosition Nth textual occurrence of the method invocation
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atInvoke(String method, int occurencePosition) {
            return at("INVOKE " + method + " " + occurencePosition);
        }

        /**
         * Rule is invoked after invocation of method within the trigger method.<br>
         * Location specifier is set as <code>AFTER INVOKE &lt;variable&gt;</code>.
         *
         * @param method  method name after which invocation the rule is executed
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterInvoke(String method) {
            return after("INVOKE " + method);
        }

        /**
         * Rule is invoked after invocation of method within the trigger method
         * where <code>occurencePosition</code> defines Nth textual occurrence of the method invocation.<br>
         * Location specifier is set as <code>AFTER INVOKE &lt;variable&gt; &lt;occurencePosition&gt;</code>.
         *
         * @param method  method name after which invocation the rule is executed
         * @param occurencePosition Nth textual occurrence of the method invocation
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterInvoke(String method, int occurencePosition) {
            return after("INVOKE " + method + " " + occurencePosition);
        }

        /**
         * Rule is invoked at entry of synchronization block in the target method.<br>
         * Location specifier is set as <code>AT SYNCHRONIZE</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atSynchronize() {
            return at("SYNCHRONIZE");
        }

        /**
         * Rule is invoked at point of invocation of method within the trigger method
         * where <code>occurencePosition</code> defines Nth textual occurrence of the method invocation.<br>
         * Location specifier is set as <code>AT SYNCHRONIZE &lt;occurencePosition&gt;</code>.
         *
         * @param occurencePosition  Nth textual occurrence of the method invocation
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atSynchronize(int occurencePosition) {
            return at("SYNCHRONIZE " + occurencePosition);
        }

        /**
         * Rule is invoked after invocation of method within the trigger method.<br>
         * Location specifier is set as <code>AFTER SYNCHRONIZE</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterSynchronize() {
            return after("SYNCHRONIZE");
        }

        /**
         * Rule is invoked after invocation of method within the trigger method
         * where <code>occurencePosition</code> defines Nth textual occurrence of the method invocation.<br>
         * Location specifier is set as <code>AFTER SYNCHRONIZE &lt;occurencePosition&gt;</code>.
         *
         * @param occurencePosition  Nth textual occurrence of the method invocation
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause afterSynchronize(int occurencePosition) {
            return after("SYNCHRONIZE " + occurencePosition);
        }

        /**
         * Identifies a throw operation within the trigger method.<br>
         * Location specifier is set as <code>AT THROW</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atThrow() {
            return at("THROW");
        }

        /**
         * Identifies a throw operation within the trigger method,
         * specified with count as Nth textual occurrence of a throw
         * inside of the method defining only that occurrence to trigger
         * execution of the rule.<br>
         * Location specifier is set as <code>AT THROW &lt;occurencePosition&gt;</code>.
         *
         * @param occurencePosition  which Nth textual occurrence of a throw triggers the rule
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atThrow(int occurencePosition) {
            return at("THROW " + occurencePosition);
        }

        /**
         * Identifies the point where a method returns control back to its caller via
         * unhandled exceptional control flow.<br>
         * Location specifier is set as <code>AT EXCEPTION EXIT</code>.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause atExceptionExit() {
            return at("EXCEPTION EXIT");
        }

        /**
         * Returning this {@link RuleConstructor} - parent in the flow api. 
         */
        public RuleConstructor parent() {
            return RuleConstructor.this;
        }

        public RuleConstructor.ConditionClause at(String at) {
            return where("AT " + at);
        }

        private RuleConstructor.ConditionClause after(String after) {
            return where("AFTER " + after);
        }
    }

    public final class ConditionClause {
        /**
         * Byteman helper class to be used in rule definition.
         *
         * @param helperClass  byteman helper class
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause helper(Class helperClass) {
            return helper(helperClass.getCanonicalName());
        }

        /**
         * Class name of Byteman helper class.
         *
         * @param helperClassName  byteman helper class name
         * @return  this, for having fluent api
         */

        public RuleConstructor.ConditionClause helper(String helperClassName) {
            RuleConstructor.this.helperName = helperClassName;
            return this;
        }

        /**
         * Definition of bind clause.<br>
         * When called as
         * <code>bind("engine:CoordinatorEngine = $0", "identifier:String = engine.getId()")</code>
         * rule looks<br>
         * <code>
         * BIND bind("engine:CoordinatorEngine = $0";
         * "identifier:String = engine.getId()
         * </code>
         *
         * @param bindClauses  bind clauses to be part of the rule
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause bind(String... bindClauses) {
            RuleConstructor.this.bind = stringifyClauses(bindClauses);
            return this;
        }

        /**
         * Setting module import definition for the rule.
         * For module import functionality works you need to use parameter
         * <code>-javaagent modules:</code>. The only provided implementation class
         * which is to manage module imports is
         * <code>org.jboss.byteman.modules.jbossmodules.JbossModulesSystem</code>
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause imports(String... imports) {
            StringBuffer importsBuf = new StringBuffer();
            for(String importString: imports) {
                importsBuf
                    .append("IMPORT ")
                    .append(importString)
                    .append(LINEBREAK);
            }
            RuleConstructor.this.imports = importsBuf.toString();
            return this;
        }

        /**
         * Defines rule for being compiled.<br>
         * Default behaviour is to use the interpreter.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause compile() {
            RuleConstructor.this.compile = "COMPILE";
            return this;
        }

        /**
         * Defines rule for not being compiled.<br>
         * Default behaviour is to use the interpreter
         * but byteman system property could be used to change
         * the default behaviour for rules being compiled every time
         * then this settings could be useful.
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ConditionClause nocompile() {
            RuleConstructor.this.compile = "NOCOMPILE";
            return this;
        }

        /**
         * Rule condition when rule will be executed.<br>
         * Defined string is directly used in rule definition.<br>
         * Rule condition is set as <code>IF &lt;condition&gt;</code>.<br>
         *
         * @param condition  rule condition string that is used for the rule
         * @return  this, for having fluent api
         */
        public RuleConstructor.ActionClause when(String condition) {
            RuleConstructor.this.when = condition;
            if(RuleConstructor.this.actionClause == null) {
                RuleConstructor.this.actionClause = RuleConstructor.this.new ActionClause();
            }
            return RuleConstructor.this.actionClause;
        }

        /**
         * Condition ensuring that rule will be executed.<br>
         * Rule condition is set as <code>IF true</code>.<br>
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ActionClause ifTrue() {
            return when("true");
        }

        /**
         * Condition ensuring that rule won't be executed.<br>
         * Rule condition is set as <code>IF false</code>.<br>
         *
         * @return  this, for having fluent api
         */
        public RuleConstructor.ActionClause ifFalse() {
            return when("false");
        }

        /**
         * Returning this {@link RuleConstructor} - parent in the flow api. 
         */
        public RuleConstructor parent() {
            return RuleConstructor.this;
        }
    }

    public final class ActionClause {
        /**
         * Definition of actions for the rule.<br>
         * When called as
         * <code>doAction("DO debug(\"killing JVM\")", "killJVM()")</code>
         * rule looks<br>
         * <code>
         * DO debug("killing JVM");
         * killJVM()
         * </code>
         *
         * @param bindClauses  bind clauses to be part of the rule
         * @return  this, for having fluent api
         */
        public RuleConstructor.Builder action(String... actions) {
            RuleConstructor.this.action = stringifyClauses(actions);
            if(RuleConstructor.this.builderClause == null) {
                RuleConstructor.this.builderClause = RuleConstructor.this.new Builder();
            }
            return RuleConstructor.this.builderClause;
        }

        /**
         * Returning this {@link RuleConstructor} - parent in the flow api. 
         */
        public RuleConstructor parent() {
            return RuleConstructor.this;
        }
    }

    public final class Builder {
        /**
         * Builds the rule and returns its representation as string.
         *
         * @return the rule as a string
         */
        public String build() {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("RULE ");
            stringBuilder.append(ruleName);
            stringBuilder.append(LINEBREAK);

            if(isInterface) {
                stringBuilder.append("INTERFACE ");
            } else {
                stringBuilder.append("CLASS ");
            }
            if(isIncludeSubclases) {
                stringBuilder.append("^");
            }
            stringBuilder.append(className);
            stringBuilder.append(LINEBREAK);

            stringBuilder.append("METHOD ");
            stringBuilder.append(methodName);
            stringBuilder.append(LINEBREAK);

            stringBuilder.append(where);
            stringBuilder.append(LINEBREAK);

            if(helperName != null) {
                stringBuilder.append("HELPER ");
                stringBuilder.append(helperName);
                stringBuilder.append(LINEBREAK);
            }

            if(imports != null) {
                stringBuilder.append(imports);
            }

            if(compile != null) {
                stringBuilder.append(compile);
                stringBuilder.append(LINEBREAK);
            }

            if(bind != null) {
                stringBuilder.append("BIND ");
                stringBuilder.append(bind);
                stringBuilder.append(LINEBREAK);
            }

            stringBuilder.append("IF ");
            stringBuilder.append(when);
            stringBuilder.append(LINEBREAK);

            stringBuilder.append("DO ");
            stringBuilder.append(action);
            stringBuilder.append(LINEBREAK);

            stringBuilder.append("ENDRULE");
            stringBuilder.append(LINEBREAK);

            return stringBuilder.toString();
        }

        /**
         * Returning this {@link RuleConstructor} - parent in the flow api. 
         */
        public RuleConstructor parent() {
            return RuleConstructor.this;
        }
    }

    String getRuleName() {
        return RuleConstructor.this.ruleName;
    }

    private String stringJoin(String join, String... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        } else if (strings.length == 1) {
            return strings[0];
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(strings[0]);
            for (int i = 1; i < strings.length; i++) {
                sb.append(join).append(strings[i]);
            }
            return sb.toString();
        }
    }

    private String stringifyClauses(String... clauses) {
        StringBuffer actionsBuffer = new StringBuffer();
        boolean isFirstAddition = true;
        boolean isSemicolon = true;

        for(String clause: clauses) {
            if(!isFirstAddition && !isSemicolon) actionsBuffer.append(";");
            if(!isFirstAddition) actionsBuffer.append(LINEBREAK);
            if(!clause.trim().endsWith(";")) isSemicolon = false;
            if(isFirstAddition) isFirstAddition = false;

            actionsBuffer.append(clause.trim());
        }
        return actionsBuffer.toString();
    }
}
