package com.geewhiz.pacify.property.resolver.araresolver;

import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlbeam.XBProjector;

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

        AraPropertyResolver araPropertyResolver = new AraPropertyResolver();

        araPropertyResolver.setInitilized(true);
        araPropertyResolver.setTarget("server1.example.org");
        araPropertyResolver.setComponent("Componente_1");
        araPropertyResolver.setNamespace("/example_namespace");

        AraData araData = new XBProjector().io().file("target/test-classes/example_ara_output_cddata.xml").read(AraData.class);
        araPropertyResolver.setAraData(araData);

        Set<String> properties = null;

        araPropertyResolver.setComponent("Componente_1");
        araPropertyResolver.setNamespace("/example_namespace");
        properties = araPropertyResolver.getProperties();
        Assert.assertEquals("Wrong property size count.", 2, properties.size());
        Assert.assertEquals("Componente_1_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_1_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));

        araPropertyResolver.setComponent("Componente_2");
        properties = araPropertyResolver.getProperties();
        Assert.assertEquals("Wrong property size count.", 2, properties.size());
        Assert.assertEquals("Componente_2_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
        Assert.assertEquals("Componente_2_foobar2_value", araPropertyResolver.getPropertyValue("foobar2"));

        araPropertyResolver.setNamespace("/another_namespace");
        properties = araPropertyResolver.getProperties();
        Assert.assertEquals("Wrong property size count.", 1, properties.size());
        Assert.assertEquals("Componente_2_another_namespace_foobar1_value", araPropertyResolver.getPropertyValue("foobar1"));
    }

}
