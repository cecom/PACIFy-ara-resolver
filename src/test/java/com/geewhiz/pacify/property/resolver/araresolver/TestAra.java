package com.geewhiz.pacify.property.resolver.araresolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlbeam.XBProjector;

import com.geewhiz.pacify.CreatePropertyFile;
import com.geewhiz.pacify.CreatePropertyFile.OutputType;
import com.geewhiz.pacify.managers.PropertyResolveManager;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.GenerateTask;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Variable;
import com.geewhiz.pacify.property.resolver.araresolver.model.GenerateTaskMixinImpl;
import com.geewhiz.pacify.property.resolver.araresolver.model.VariableMixinImpl;
import com.geewhiz.pacify.resolver.PropertyResolver;
import com.geewhiz.pacify.test.ListAppender;
import com.geewhiz.pacify.test.TestUtil;
import com.geewhiz.pacify.utils.LoggingUtils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class TestAra {

    @Test
    public void readTest() throws IOException {
        Logger logger = LogManager.getLogger(TestAra.class.getName());
        LoggingUtils.setLogLevel(logger, Level.DEBUG);

        AraPropertyResolver araPropertyResolver = createAraPropertyResolver();

        Set<String> properties = null;

        araPropertyResolver.setComponent("Componente_1");
        araPropertyResolver.setNamespace("/example_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 3, properties.size());
        Assert.assertEquals("expect for value1", "Componente_1_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("expect for value2", "Componente_1_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));
        Assert.assertEquals("expect for value3", "encryptedPassword", araPropertyResolver.getPropertyValue("foobar3"));

        araPropertyResolver.setComponent("Componente_2");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 2, properties.size());
        Assert.assertEquals("Componente_2_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_2_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));

        araPropertyResolver.setNamespace("/another_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 1, properties.size());
        Assert.assertEquals("Componente_2_another_namespace_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
    }

    @Test
    public void callTest() throws IOException {
        Logger logger = LogManager.getLogger(TestAra.class.getName());
        LoggingUtils.setLogLevel(logger, Level.DEBUG);

        AraPropertyResolver araPropertyResolver = createAraPropertyResolver();
        PropertyResolveManager prm = createPropertyResolveManager(araPropertyResolver);
        CreatePropertyFile createPropertyFile = new CreatePropertyFile(prm);

        createPropertyFile.setOutputType(OutputType.Stdout);
        createPropertyFile.setOutputEncoding("utf-8");
        createPropertyFile.setOutputPrefix("");
        createPropertyFile.setFilemode("400");

        araPropertyResolver.setComponent("Componente_1");
        araPropertyResolver.setNamespace("/example_namespace");

        ListAppender listAppender = TestUtil.addListAppenderToLogger();
        PrintStream oldStdOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            createPropertyFile.write();
        }
        finally {
            System.setOut(oldStdOut);
            outContent.close();
        }

        // Test the DEBUG LOG Messages, we should not see the encrypted value
        Assert.assertEquals("We expect log lines:", 6, listAppender.getLogMessages().size());
        Assert.assertEquals("Password should be encrypted in debug message.", "       Resolved property [foobar3] to value [**********]", listAppender
                .getLogMessages().get(4));

        // Test the STDOUT there we should see the encrypted value
        List<String> outputLines = IOUtils.readLines(new StringReader(outContent.toString()));
        Assert.assertEquals("We expect the decoded value", "foobar3=encryptedPassword", outputLines.get(2));
    }

    private AraPropertyResolver createAraPropertyResolver() throws IOException {
        AraPropertyResolver araPropertyResolver = new AraPropertyResolver();

        araPropertyResolver.setInitilized(true);
        araPropertyResolver.setTarget("server1.example.org");
        araPropertyResolver.setDecodePasswordWithBase64(true);
        araPropertyResolver.setBeginToken("@");
        araPropertyResolver.setEndToken("@");

        XBProjector xbProjector = new XBProjector();
        xbProjector.mixins().addProjectionMixin(Variable.class, new VariableMixinImpl("=>", "UTF-8", Boolean.TRUE));
        xbProjector.mixins().addProjectionMixin(GenerateTask.class, new GenerateTaskMixinImpl("=>"));

        AraData araData = xbProjector.io().file("target/test-classes/example_ara_output_cddata.xml").read(AraData.class);
        araPropertyResolver.setAraData(araData);
        return araPropertyResolver;
    }

    private PropertyResolveManager createPropertyResolveManager(AraPropertyResolver apr) {
        Set<PropertyResolver> propertyResolverList = new TreeSet<PropertyResolver>();
        propertyResolverList.add(apr);
        PropertyResolveManager prm = new PropertyResolveManager(propertyResolverList);
        return prm;
    }

}
