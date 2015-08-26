package com.geewhiz.pacify.property.resolver.araresolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.geewhiz.pacify.defect.Defect;
import com.geewhiz.pacify.property.resolver.araresolver.defects.AraParameterMissingDefect;
import com.geewhiz.pacify.resolver.PropertyResolver;
import com.geewhiz.pacify.resolver.PropertyResolverModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

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

public class AraPropertyResolverModule extends PropertyResolverModule {

    Map<String, String>  commandLineParameters;
    private String       araUrl;
    private String       username;
    private String       password;
    private String       runId;
    private String       target;
    private String       component;
    private String       namespace;
    private String       beginToken;
    private String       endToken;
    private String       propertyKeyValueSeparator;
    private boolean      decodePasswordWithBase64;

    private List<Defect> defects = new ArrayList<Defect>();

    @Override
    public String getResolverId() {
        return "AraResolver";
    }

    @Override
    protected void configure() {
        Multibinder<PropertyResolver> resolveBinder = Multibinder.newSetBinder(binder(), PropertyResolver.class);
        resolveBinder.addBinding().to(AraPropertyResolver.class);
    }

    @Override
    public void setParameters(Map<String, String> commandLineParameters) {
        this.commandLineParameters = commandLineParameters;

        araUrl = commandLineParameters.get("araUrl");
        username = commandLineParameters.get("username");
        password = commandLineParameters.get("password");
        runId = commandLineParameters.get("runId");
        target = commandLineParameters.get("target");
        component = commandLineParameters.get("component");
        namespace = commandLineParameters.get("namespace");
        beginToken = commandLineParameters.get("beginToken");
        endToken = commandLineParameters.get("endToken");
        propertyKeyValueSeparator = commandLineParameters.get("propertyKeySeparator");
        decodePasswordWithBase64 = Boolean.parseBoolean(commandLineParameters.get("decodePasswordWithBase64"));

        checkNotNull(araUrl, "araUrl is null! Please specify it via -RAraResolver.araUrl=<url>");
        checkNotNull(username, "username is null! Please specify it via -RAraResolver.username=<username>. The user is used to authenticate with ara.");
        checkNotNull(password, "password is null! Please specify it via -RAraResolver.password=<password>. The password is used to authenticate with ara.");
        checkNotNull(runId, "runId is null! Please specify it via -RAraResolver.runId=<runId>.");
        checkNotNull(target, "target is null! Please specify it via -RAraResolver.target=<target>.");
        checkNotNull(component, "component is null! Please specify it via -RAraResolver.component=<component>.");
        checkNotNull(namespace, "namespace is null! Please specify it via -RAraResolver.namespace=<namespace>.");
    }

    @Provides
    public AraPropertyResolver createFilePropertyResolver() {
        AraPropertyResolver araPropertyResolver = new AraPropertyResolver();

        araPropertyResolver.setAraUrl(araUrl);
        araPropertyResolver.setUserName(username);
        araPropertyResolver.setPassword(password);
        araPropertyResolver.setRunId(Integer.parseInt(runId));
        araPropertyResolver.setTarget(target);
        araPropertyResolver.setComponent(component);
        araPropertyResolver.setNamespace(namespace);
        araPropertyResolver.setDecodePasswordWithBase64(decodePasswordWithBase64);
        araPropertyResolver.setBeginToken(beginToken == null ? "@" : beginToken);
        araPropertyResolver.setEndToken(endToken == null ? "@" : endToken);
        araPropertyResolver.setKeyValueSeparatorToken(propertyKeyValueSeparator == null ? "=>" : propertyKeyValueSeparator);

        return araPropertyResolver;
    }

    @Override
    public List<Defect> getDefects() {
        return defects;
    }

    private void checkNotNull(String param, String message) {
        if (param == null) {
            defects.add(new AraParameterMissingDefect(param, message));
        }
    }

}
