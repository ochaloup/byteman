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

import org.junit.Test;
import junit.framework.Assert;

public class RuleBuilderTestCase {

    @Test
    public void basic() {
        String testRule = String.format(
            "RULE basic rule%n" +
            "CLASS javax.transaction.xa.XAResource%n" +
            "METHOD commit%n" +
            "AT ENTRY%n" +
            "IF NOT flagged(\"commitFlag\")%n" +
            "DO throw new javax.transaction.xa.XAResource(100)%n" +
            "ENDRULE%n");

        String rule = new RuleConstructor("basic rule")
            .onClass("javax.transaction.xa.XAResource")
            .inMethod("commit")
            .atEntry()
            .when("NOT flagged(\"commitFlag\")")
            .action("throw new javax.transaction.xa.XAResource(100)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, rule);
    }

    @Test
    public void basicWithSubclasses() {
        String testRule = String.format(
            "RULE basic rule%n" +
            "CLASS ^javax.transaction.xa.XAResource%n" +
            "METHOD rollback%n" +
            "AT EXIT%n" +
            "IF NOT flagged(\"commitFlag\")%n" +
            "DO throw new javax.transaction.xa.XAResource(100)%n" +
            "ENDRULE%n");

        String rule = new RuleConstructor("basic rule")
            .onClass("javax.transaction.xa.XAResource")
            .includeSubclases()
            .inMethod("rollback")
            .atExit()
            .when("NOT flagged(\"commitFlag\")")
            .action("throw new javax.transaction.xa.XAResource(100)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, rule);
    }

    @Test
    public void withBind() {
        String testRule = String.format(
            "RULE bind rule%n" +
            "CLASS org.my.BoundedBuffer%n" +
            "METHOD <init>(int)%n" +
            "AT EXIT%n" +
            "HELPER org.jboss.MyHelper%n" +
            "BIND buffer = $0;%n" +
            "size = $1%n" +
            "IF $1 < 100%n" +
            "DO createCountDown(buffer, size - 1)%n" +
            "ENDRULE%n");

        RuleConstructor.Builder builder = new RuleConstructor("bind rule")
            .onClass("org.my.BoundedBuffer")
            .inConstructor("int")
            .atExit()
            .helper("org.jboss.MyHelper")
            .bind("buffer = $0", "size = $1")
            .when("$1 < 100")
            .action("createCountDown(buffer, size - 1)");

        Assert.assertEquals("The rule does not match the built one", testRule, builder.build());
    }

    @Test
    public void atLine() {
        String testRule = String.format(
            "RULE commit with no arguments on wst11 coordinator engine%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD State commit()%n" +
            "AT LINE 324%n" +
            "IF false%n" +
            "DO traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("commit with no arguments on wst11 coordinator engine")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("State commit()")
            .atLine(324)
            .ifFalse()
            .action("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void atInvoke() {
        String testRule = String.format(
            "RULE at invoke%n" +
            "CLASS CoordinatorEngine%n" +
            "METHOD commit%n" +
            "AT INVOKE sendCommit%n" +
            "IF false%n" +
            "DO traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("at invoke")
            .onClass("CoordinatorEngine")
            .inMethod("commit")
            .atInvoke("sendCommit")
            .ifFalse()
            .action("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void afterRead() {
        String testRule = String.format(
            "RULE at invoke%n" +
            "CLASS CoordinatorEngine%n" +
            "METHOD commit%n" +
            "AFTER READ $current%n" +
            "IF false%n" +
            "DO traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("at invoke")
            .onClass("CoordinatorEngine")
            .inMethod("commit")
            .afterRead("$current")
            .ifFalse()
            .action("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void afterWrite() {
        String testRule = String.format(
            "RULE at invoke%n" +
            "CLASS CoordinatorEngine%n" +
            "METHOD commit%n" +
            "AFTER WRITE $current%n" +
            "IF false%n" +
            "DO traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("at invoke")
            .onClass("CoordinatorEngine")
            .inMethod("commit")
            .afterWrite("$current")
            .ifFalse()
            .action("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void atSynchronize() {
        String testRule = String.format(
            "RULE at invoke%n" +
            "CLASS CoordinatorEngine%n" +
            "METHOD commit%n" +
            "AT SYNCHRONIZE%n" +
            "IF false%n" +
            "DO traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("at invoke")
            .onClass("CoordinatorEngine")
            .inMethod("commit")
            .atSynchronize()
            .ifFalse()
            .action("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void forInterface() {
        String testRule = String.format(
            "RULE commit with no arguments on any engine%n" +
            "INTERFACE com.arjuna.wst11.messaging.engines.Engine%n" +
            "METHOD commit()%n" +
            "AT THROW%n" +
            "IF true%n" +
            "DO System.out.println(\"One ring\");%n" +
            "System.out.println(\"rule them all\")%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("commit with no arguments on any engine")
            .onInterface("com.arjuna.wst11.messaging.engines.Engine")
            .inMethod("commit()")
            .atThrow()
            .ifTrue()
            .action(
                "System.out.println(\"One ring\")",
                "System.out.println(\"rule them all\")")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void doThrow() {
        String testRule = String.format(
            "RULE countdown at commit%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD commit%n" +
            "AT READ state%n" +
            "IF true%n" +
            "DO debug(\"throwing wrong state\");%n" +
            "throw new WrongStateException()%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("countdown at commit")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("commit")
            .where("AT READ state")
            .ifTrue()
            .action(
                "debug(\"throwing wrong state\")",
                "throw new WrongStateException()")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void importNocompile() {
        String testRule = String.format(
            "RULE compile import example%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD prepare%n" +
            "AT ENTRY%n" +
            "IMPORT javax.transaction.api%n" +
            "NOCOMPILE%n" +
            "IF true%n" +
            "DO org.my.Logger.log(runnableKlazz, System.currentTimeMillis())%n" +
            "ENDRULE%n");

        String builtRule = new RuleConstructor("compile import example")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("prepare")
            .atEntry()
            .nocompile()
            .imports("javax.transaction.api")
            .ifTrue()
            .action("org.my.Logger.log(runnableKlazz, System.currentTimeMillis())")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void incompleteRule() {
        String incompleteRule = String.format(
            "RULE compile import example%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD prepare%n" +
            "AT ENTRY%n" +
            "NOCOMPILE");

        RuleConstructor builder = new RuleConstructor("compile import example")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("prepare")
            .atEntry()
            .nocompile()
            .parent();

        try {
            builder.build();
        } catch (IllegalStateException ise) {
            Assert.assertTrue("Caught illegal state exception " + ise + " should contain content of incomplete rule",
                    ise.getMessage().contains(incompleteRule));
            return;
        }
        Assert.fail("IllegalStateException on incomplete rule was expected");
    }
}
