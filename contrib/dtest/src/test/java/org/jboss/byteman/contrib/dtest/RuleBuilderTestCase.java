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
            "DO%n" +
            "throw new javax.transaction.xa.XAResource(100)%n" +
            "ENDRULE%n");

        String rule = new RuleBuilder("basic rule")
            .onClass("javax.transaction.xa.XAResource")
            .inMethod("commit")
            .atEntry()
            .when("NOT flagged(\"commitFlag\")")
            .doAction("throw new javax.transaction.xa.XAResource(100)")
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
            "BIND%n" +
            "buffer = $0;%n" +
            "size = $1%n" +
            "IF $1 < 100%n" +
            "DO%n" +
            "createCountDown(buffer, size - 1)%n" +
            "ENDRULE%n");

        RuleBuilder builder = new RuleBuilder("bind rule")
            .onClass("org.my.BoundedBuffer")
            .inConstructor("int")
            .atExit()
            .bind("buffer = $0")
            .bind("size = $1")
            .when("$1 < 100")
            .doAction("createCountDown(buffer, size - 1)");

        Assert.assertEquals("The rule does not match the built one", testRule, builder.build());
    }

    @Test
    public void atLine() {
        String testRule = String.format(
            "RULE commit with no arguments on wst11 coordinator engine%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD State commit()%n" +
            "AT LINE 324%n" +
            "IF true%n" +
            "DO%n" +
            "traceStack(\"dump\", 20)%n" +
            "ENDRULE%n");

        String builtRule = new RuleBuilder("commit with no arguments on wst11 coordinator engine")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("State commit()")
            .atLine(324)
            .when(true)
            .doAction("traceStack(\"dump\", 20)")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void forInterface() {
        String testRule = String.format(
            "RULE commit with no arguments on any engine%n" +
            "INTERFACE com.arjuna.wst11.messaging.engines.Engine%n" +
            "METHOD commit()%n" +
            "AT THROW ALL%n" +
            "IF true%n" +
            "DO%n" +
            "System.out.println(\"One ring\");%n" +
            "System.out.println(\"rule them all\")%n" +
            "ENDRULE%n");

        String builtRule = new RuleBuilder("commit with no arguments on any engine")
            .onInterface("com.arjuna.wst11.messaging.engines.Engine")
            .inMethod("commit()")
            .atThrow()
            .when(true)
            .doAction(
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
            "DO%n" +
            "debug(\"throwing wrong state\");%n" +
            "throw new WrongStateException()%n" +
            "ENDRULE%n");

        String builtRule = new RuleBuilder("countdown at commit")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("commit")
            .where("AT READ state")
            .when(true)
            .doAction("debug(\"throwing wrong state\")")
            .doAction("throw new WrongStateException()")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
    }

    @Test
    public void importNocompile() {
        String testRule = String.format(
            "RULE compile import example%n" +
            "CLASS com.arjuna.wst11.messaging.engines.CoordinatorEngine%n" +
            "METHOD prepare%n" +
            "IMPORT javax.transaction.api%n" +
            "NOCOMPILE%n" +
            "AT ENTRY%n" +
            "IF true%n" +
            "DO%n" +
            "org.my.Logger.log(runnableKlazz, System.currentTimeMillis())%n" +
            "ENDRULE%n");

        String builtRule = new RuleBuilder("compile import example")
            .onClass("com.arjuna.wst11.messaging.engines.CoordinatorEngine")
            .inMethod("prepare")
            .atEntry()
            .nocompile()
            .addImport("javax.transaction.api")
            .whenTrue()
            .doAction("org.my.Logger.log(runnableKlazz, System.currentTimeMillis())")
            .build();

        Assert.assertEquals("The rule does not match the built one", testRule, builtRule);
        
    }
}
