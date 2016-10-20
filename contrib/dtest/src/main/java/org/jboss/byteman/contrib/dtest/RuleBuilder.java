/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2010,
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
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */
public class RuleBuilder {
    private static final String LINEBREAK = String.format("%n");

    private static final String CONSTRUCTOR_METHOD = "<init>";
    private static final String CLASS_CONSTRUCTOR = "<clinit>";

    private String ruleName;
    private String className;
    private boolean isInterface;
    private boolean isIncludeSubclases;
    private String methodName;
    private String helperName;
    private String whereClause = "AT ENTRY";
    private String bindClause;
    private String ifClause = "true";
    private String doClause;
    private String importClause;
    private String compileClause;

    /**
     * Rule builder initialization.
     *
     * @param ruleName  name of rule is demanded information
     */
    public RuleBuilder(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * Class that rule event is associated to.
     *
     * @param clazz  class as target of rule injection
     * @return this, for having fluent api
     */
    public RuleBuilder onClass(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), false);
    }

    /**
     * Class name that rule event is associated to.
     *
     * @param className  class name as target of rule injection
     * @return this, for having fluent api
     */
    public RuleBuilder onClass(String className) {
        return onSpecifier(className, false);
    }

    /**
     * Interface class that rule event is associated to.
     *
     * @param clazz  interface class as target of rule injection
     * @return this, for having fluent api
     */
    public RuleBuilder onInterface(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), true);
    }

    /**
     * Interface class name that rule event is associated to.
     *
     * @param className interface class name as target of rule injection
     * @return this, for having fluent api
     */
    public RuleBuilder onInterface(String className) {
        return onSpecifier(className, true);        
    }

    /**
     * Defining that the rule will be injected to all sub-classes
     * or classes implementing the interface.<br>
     * By default byteman injects the rule only to the specified
     * class and it left children classes untouched.<br>
     * The rule class definition is changed to <code>CLASS ^org.jboss.byteman.ExampleClass</code>.
     *
     * @return this, for having fluent api
     */
    public RuleBuilder includeSubclases() {
        this.isIncludeSubclases = true;
        return this;
    }

    /**
     * Defining method where the rule is injected to.
     *
     * @param methodName  method name for rule injection
     * @return this, for having fluent api
     */
    public RuleBuilder inMethod(String methodName) {
        this.methodName = methodName;
        return this;
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
    public RuleBuilder inMethod(String methodName, String... argTypes) {
        this.methodName = methodName + "(" + stringJoin(",", argTypes) + ")";
        return this;
    }

    /**
     * Defining constructor, special method type,
     * as place for rule injection.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder inConstructor() {
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
    public RuleBuilder inConstructor(String... argTypes) {
        return inMethod(CONSTRUCTOR_METHOD, argTypes);
    }

    /**
     * Defining class initialization method as place for rule injection.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder inClassInitMethod() {
        return inMethod(CLASS_CONSTRUCTOR);
    }

    /**
     * Defining class initialization method as place for rule injection.
     *
     * @param argTypes  method argument types to closer specify which method
     * @return  this, for having fluent api
     */
    public RuleBuilder inClassInitMethod(String... argTypes) {
        return inMethod(CLASS_CONSTRUCTOR, argTypes);
    }

    /**
     * Byteman helper class to be used in rule definition.
     *
     * @param helperClass  byteman helper class
     * @return  this, for having fluent api
     */
    public RuleBuilder usingHelper(Class helperClass) {
        return usingHelper(helperClass.getCanonicalName());
    }

    /**
     * Class name of Byteman helper class.
     *
     * @param helperClassName  byteman helper class name
     * @return  this, for having fluent api
     */

    public RuleBuilder usingHelper(String helperClassName) {
        this.helperName = helperClassName;
        return this;
    }

    /**
     * Location specifier definition.<br>
     * Defined string is directly used in rule definition.
     *
     * @param where  location specifier
     * @return  this, for having fluent api
     */
    public RuleBuilder where(String where) {
        whereClause = where;
        return this;
    }

    /**
     * Rule is invoked at entry point of method.<br>
     * Location specifier is set as <code>AT ENTRY</code>.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder atEntry() {
        return at("ENTRY");
    }

    /**
     * Rule is invoked at exit point of method.<br>
     * Location specifier is set as <code>AT EXIT</code>.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder atExit() {
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
    public RuleBuilder atLine(int line) {
        return at("LINE " + line);
    }

    /**
     * Identifies a throw operation within the trigger method.<br>
     * Location specifier is set as <code>AT THROW ALL</code>.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder atThrow() {
        return at("THROW ALL");
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
    public RuleBuilder atThrow(int occurencePosition) {
        return at("THROW " + occurencePosition);
    }

    /**
     * Identifies the point where a method returns control back to its caller via
     * unhandled exceptional control flow.<br>
     * Location specifier is set as <code>AT EXCEPTION EXIT</code>.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder atExceptionExit() {
        return at("EXCEPTION EXIT");
    }

    /**
     * Location specifier definition prefixed with <code>AT</code>.<br>
     *
     * @param at  location specifier complement of <code>AT</code>
     * @return  this, for having fluent api
     */
    public RuleBuilder at(String at) {
        return where("AT " + at);
    }

    /**
     * Rule condition when rule will be executed.<br>
     * Defined string is directly used in rule definition.
     *
     * @param condition  rule condition string that is used for the rule
     * @return  this, for having fluent api
     */
    public RuleBuilder when(String condition) {
        ifClause = condition;
        return this;
    }

    /**
     * Condition ensuring that rule will be executed.<br>
     * Rule condition is set as <code>IF true</code>.<br>
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder whenTrue() {
        return when("true");
    }

    /**
     * Condition ensuring that rule won't be executed.<br>
     * Rule condition is set as <code>IF false</code>.<br>
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder whenFalse() {
        return when("false");
    }

    /**
     * Rule condition which defines if rule will be executed (true)
     * or won't be (false).
     * Rule condition is set as <code>IF &lt;when&gt;</code>.<br>
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder when(boolean when) {
        return when("" + when);
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
    public RuleBuilder bind(String... bindClauses) {
        this.bindClause = stringifyClauses(bindClauses);
        return this;
    }

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
    public RuleBuilder doAction(String... actions) {
        this.doClause = stringifyClauses(actions);
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
    public RuleBuilder imports(String... imports) {
        StringBuffer importsBuf = new StringBuffer();
        for(String importString: imports) {
            importsBuf
                .append("IMPORT ")
                .append(importString)
                .append(LINEBREAK);
        }
        this.importClause = importsBuf.toString();
        return this;
    }

    /**
     * Defines rule for being compiled.<br>
     * Default behaviour is to use the interpreter.
     *
     * @return  this, for having fluent api
     */
    public RuleBuilder compile() {
        this.compileClause = "COMPILE";
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
    public RuleBuilder nocompile() {
        this.compileClause = "NOCOMPILE";
        return this;
    }

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

        stringBuilder.append(whereClause);
        stringBuilder.append(LINEBREAK);

        if(helperName != null) {
            stringBuilder.append("HELPER ");
            stringBuilder.append(helperName);
            stringBuilder.append(LINEBREAK);
        }

        if(importClause != null) {
            stringBuilder.append(importClause);
        }

        if(compileClause != null) {
            stringBuilder.append(compileClause);
            stringBuilder.append(LINEBREAK);
        }

        if(bindClause != null) {
            stringBuilder.append("BIND ");
            stringBuilder.append(bindClause);
            stringBuilder.append(LINEBREAK);
        }

        stringBuilder.append("IF ");
        stringBuilder.append(ifClause);
        stringBuilder.append(LINEBREAK);

        stringBuilder.append("DO ");
        stringBuilder.append(doClause);
        stringBuilder.append(LINEBREAK);

        stringBuilder.append("ENDRULE");
        stringBuilder.append(LINEBREAK);

        return stringBuilder.toString();
    }

    /**
     * Builds the rule and returns its representation as string.<br>
     * This has the same functionality as method {@link #build()}. 
     *
     * @return the rule as a string
     */
    @Override
    public String toString() {
        return build();
    }

    String getRuleName() {
        return ruleName;
    }

    private RuleBuilder onSpecifier(String className, boolean isInterface) {
        this.className = className;
        this.isInterface = isInterface;
        return this;
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
