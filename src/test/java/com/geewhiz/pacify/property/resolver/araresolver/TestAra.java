package com.geewhiz.pacify.property.resolver.araresolver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import com.geewhiz.pacify.Validator;
import com.geewhiz.pacify.defect.Defect;
import com.geewhiz.pacify.defect.ResolverDefect;
import com.geewhiz.pacify.managers.EntityManager;
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

        AraPropertyResolver araPropertyResolver = null;

        Set<String> properties = null;

        araPropertyResolver=createAraPropertyResolver("success", "Componente_1", "/example_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 3, properties.size());
        Assert.assertEquals("expect for value1", "Componente_1_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("expect for value2", "Componente_1_foobar2_value_with_<_>_<", araPropertyResolver.getPropertyValue("foobar2"));
        Assert.assertEquals("expect for value3", "encryptedPassword", araPropertyResolver.getPropertyValue("foobar3"));

        
        araPropertyResolver = createAraPropertyResolver("success", "Componente_2", "/example_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 2, properties.size());
        Assert.assertEquals("Componente_2_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_2_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));

        araPropertyResolver = createAraPropertyResolver("success", "Componente_2", "/another_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 2, properties.size());
        Assert.assertEquals("Componente_2_another_namespace_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_2_another_namespace_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));

        araPropertyResolver = createAraPropertyResolver("success", "Componente_3", "/example_namespace");
        properties = araPropertyResolver.getPropertyKeys();
        Assert.assertEquals("Wrong property size count.", 1000, properties.size());
        Assert.assertEquals("Componente_3_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_3_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));
        Assert.assertEquals("Componente_3_foobar1000_value", araPropertyResolver.getPropertyValue("foobar1000"));
    }

    @Test
    public void callTest() throws IOException {
        Logger logger = LogManager.getLogger(TestAra.class.getName());
        LoggingUtils.setLogLevel(logger, Level.DEBUG);

        AraPropertyResolver araPropertyResolver = createAraPropertyResolver("success", "Componente_1", "/example_namespace");
        PropertyResolveManager prm = createPropertyResolveManager(araPropertyResolver);
        CreatePropertyFile createPropertyFile = new CreatePropertyFile(prm);

        createPropertyFile.setOutputType(OutputType.Stdout);
        createPropertyFile.setOutputEncoding("utf-8");
        createPropertyFile.setOutputPrefix("");
        createPropertyFile.setFilemode("400");

        ListAppender listAppender = TestUtil.addListAppenderToLogger();
        PrintStream oldStdOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            createPropertyFile.write();
        } finally {
            System.setOut(oldStdOut);
            outContent.close();
        }

        // Test the DEBUG LOG Messages, we should not see the encrypted value
        Assert.assertEquals("We expect log lines:", 6, listAppender.getLogMessages().size());
        Assert.assertEquals("Password should be encrypted in debug message.", "             Resolved property [foobar3] to value [**********]",
                listAppender.getLogMessages().get(4));

        // Test the STDOUT there we should see the encrypted value
        List<String> outputLines = IOUtils.readLines(new StringReader(outContent.toString()));
        Assert.assertEquals("We expect the decoded value", "*foobar3=encryptedPassword", outputLines.get(0));
    }

    @Test
    public void errorTest() throws IOException {
        Logger logger = LogManager.getLogger(TestAra.class.getName());
        LoggingUtils.setLogLevel(logger, Level.INFO);

        AraPropertyResolver araPropertyResolver = createAraPropertyResolver("error/test1", "Componente_1", "/example_namespace");
        PropertyResolveManager prm = createPropertyResolveManager(araPropertyResolver);

        EntityManager entityManager = new EntityManager(new File("target/test-classes/error/test1/package"));
        entityManager.initialize();

        Validator validator = new Validator(prm);
        validator.enablePropertyResolveChecks();
        LinkedHashSet<Defect> result = validator.validateInternal(entityManager);

        List<Defect> defects = new ArrayList<Defect>(result);

        Assert.assertEquals("We expect one error", 1, defects.size());
        Assert.assertEquals("We expect one error",
                "        You defined to decode the value with base64, but this value isn't base64 encoded. [AraPropertyName=/example_namespace/value3],[Property=foobar3] [Value=**********]",
                ((ResolverDefect) defects.get(0)).getMessage());
    }

    @Test
    public void validateTest() throws IOException {
        Logger logger = LogManager.getLogger(TestAra.class.getName());
        LoggingUtils.setLogLevel(logger, Level.INFO);

        AraPropertyResolver araPropertyResolver = createAraPropertyResolver("success", "Componente_3", "/example_namespace");
        PropertyResolveManager prm = createPropertyResolveManager(araPropertyResolver);

        EntityManager entityManager = new EntityManager(new File("target/test-classes/success/package"));
        entityManager.initialize();

        Validator validator = new Validator(prm);
        validator.enablePropertyResolveChecks();
        LinkedHashSet<Defect> result = validator.validateInternal(entityManager);

        List<Defect> defects = new ArrayList<Defect>(result);

        Assert.assertEquals("We expect no error", 0, defects.size());
    }

    private AraPropertyResolver createAraPropertyResolver(String folder, String component, String namespace) throws IOException {
        AraPropertyResolver araPropertyResolver = new AraPropertyResolver();

        araPropertyResolver.setInitilized(true);
        araPropertyResolver.setTarget("server1.example.org");
        araPropertyResolver.setDecodePasswordWithBase64(true);
        araPropertyResolver.setBeginToken("@");
        araPropertyResolver.setEndToken("@");
        araPropertyResolver.setComponent(component);
        araPropertyResolver.setNamespace(namespace);

        XBProjector xbProjector = new XBProjector();
        xbProjector.mixins().addProjectionMixin(Variable.class, new VariableMixinImpl("=>", "UTF-8", Boolean.TRUE));
        xbProjector.mixins().addProjectionMixin(GenerateTask.class, new GenerateTaskMixinImpl("=>"));

        AraData araData = xbProjector.io().file("target/test-classes/" + folder + "/response/example_ara_output_cddata.xml").read(AraData.class);
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
