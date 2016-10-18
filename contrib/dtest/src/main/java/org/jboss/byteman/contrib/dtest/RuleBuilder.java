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
    private String methodName;
    private String helperName;
    private String whereClause = "AT ENTRY";
    private String bindClause;
    private String ifClause = "true";
    private String doClause;
    private String importClause;
    private String compileClause;

    public RuleBuilder(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleBuilder onClass(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), false);
    }

    public RuleBuilder onClass(String className) {
        return onSpecifier(className, false);
    }

    public RuleBuilder onInterface(Class clazz) {
        return onSpecifier(clazz.getCanonicalName(), true);
    }

    public RuleBuilder onInterface(String className) {
        return onSpecifier(className, true);        
    }

    public RuleBuilder inMethod(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public RuleBuilder inMethod(String methodName, String... args) {
        this.methodName = methodName + "(" + stringJoin(",", args) + ")";
        return this;
    }

    public RuleBuilder inConstructor() {
        return inMethod(CONSTRUCTOR_METHOD);
    }

    public RuleBuilder inConstructor(String... args) {
        return inMethod(CONSTRUCTOR_METHOD, args);
    }

    public RuleBuilder inClassInit() {
        return inMethod(CLASS_CONSTRUCTOR);
    }

    public RuleBuilder inClassInit(String... args) {
        return inMethod(CLASS_CONSTRUCTOR, args);
    }

    public RuleBuilder usingHelper(Class helperClass) {
        return usingHelper(helperClass.getCanonicalName());
    }

    public RuleBuilder usingHelper(String helperName) {
        this.helperName = helperName;
        return this;
    }

    public RuleBuilder where(String where) {
        whereClause = where;
        return this;
    }

    public RuleBuilder atEntry() {
        return at("ENTRY");
    }

    public RuleBuilder atExit() {
        return at("EXIT");
    }

    public RuleBuilder atLine(int line) {
        return at("LINE " + line);
    }

    public RuleBuilder atThrow() {
        return at("THROW ALL");
    }

    public RuleBuilder atThrow(int count) {
        return at("THROW " + count);
    }

    public RuleBuilder atExceptionExit() {
        return at("EXCEPTION EXIT");
    }

    public RuleBuilder at(String at) {
        return where("AT " + at);
    }

    public RuleBuilder when(String condition) {
        ifClause = condition;
        return this;
    }

    public RuleBuilder whenTrue() {
        return when("true");
    }

    public RuleBuilder whenFalse() {
        return when("false");
    }

    public RuleBuilder when(boolean when) {
        return when("" + when);
    }

    public RuleBuilder bind(String... bindClauses) {
        if(this.bindClause == null) this.bindClause = "";
        for(String bindClause: bindClauses) {
            if(!this.bindClause.isEmpty() && !this.bindClause.trim().endsWith(";")) {
                this.bindClause += ";";
            }
            this.bindClause += LINEBREAK + bindClause;
        }
        return this;
    }

    public RuleBuilder doAction(String... actions) {
        if(this.doClause == null) this.doClause = "";
        for(String action: actions) {
            if(!this.doClause.isEmpty() && !this.doClause.trim().endsWith(";")) {
                doClause += ";";
            }
            doClause += LINEBREAK + action;
        }
        return this;
    }

    public RuleBuilder addImport(String... imports) {
        if(this.importClause == null) this.importClause = "";
        for(String importString: imports) {
            importClause += "IMPORT " + importString + LINEBREAK;
        }
        return this;
    }
    
    public RuleBuilder compile() {
        this.compileClause = "COMPILE";
        return this;
    }
    
    public RuleBuilder nocompile() {
        this.compileClause = "NOCOMPILE";
        return this;
    }

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
        stringBuilder.append(className);
        stringBuilder.append(LINEBREAK);

        stringBuilder.append("METHOD ");
        stringBuilder.append(methodName);
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

        stringBuilder.append(whereClause);
        stringBuilder.append(LINEBREAK);

        if(bindClause != null) {
            stringBuilder.append("BIND");
            stringBuilder.append(bindClause);
            stringBuilder.append(LINEBREAK);
        }

        stringBuilder.append("IF ");
        stringBuilder.append(ifClause);
        stringBuilder.append(LINEBREAK);

        stringBuilder.append("DO");
        stringBuilder.append(doClause);
        stringBuilder.append(LINEBREAK);

        stringBuilder.append("ENDRULE");
        stringBuilder.append(LINEBREAK);

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return build();
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
}
