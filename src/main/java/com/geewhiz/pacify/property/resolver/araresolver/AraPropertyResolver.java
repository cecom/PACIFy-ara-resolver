package com.geewhiz.pacify.property.resolver.araresolver;

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

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlbeam.XBProjector;

import com.geewhiz.pacify.defect.Defect;
import com.geewhiz.pacify.property.resolver.BasePropertyResolver;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.GenerateTask;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Task;
import com.geewhiz.pacify.property.resolver.araresolver.model.AraData.Variable;
import com.geewhiz.pacify.property.resolver.araresolver.model.GenerateTaskMixinImpl;
import com.geewhiz.pacify.property.resolver.araresolver.model.VariableMixinImpl;
import com.uc4.ara.feature.utils.Maxim;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentDescriptorResult;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentService;
import com.uc4.schemas.bond._2011_01.deploymentservice.DeploymentService_Service;

public class AraPropertyResolver extends BasePropertyResolver {

    private final Logger                logger        = LogManager.getLogger(AraPropertyResolver.class.getName());

    private static final String         SERVICE_NAME  = "service/DeploymentService.svc";

    private String                      araUrl;
    private String                      userName;
    private String                      password;
    private Integer                     runId;
    private String                      target;
    private String                      component;
    private String                      namespace;

    private boolean                     initilized    = false;
    private AraData                     araData;

    private Boolean                     decodePasswordWithBase64;

    private String                      beginToken;
    private String                      endToken;

    private String                      propertyKeyValueSeparator;

    private TreeSet<String>             propertyKeys;
    private final Map<String, Variable> variableCache = new HashMap<String, Variable>();

    public AraPropertyResolver() {
    }

    private synchronized void initialize() {
        if (isInitilized()) {
            return;
        }

        if (araUrl == null) {
            throw new IllegalArgumentException("Ara URL is null!");
        }

        if (userName == null) {
            throw new IllegalArgumentException("Ara Username is null!");
        }

        if (password == null) {
            throw new IllegalArgumentException("Ara User Password is null!");
        }

        if (runId == null) {
            throw new IllegalArgumentException("Ara Run Id is null!");
        }

        if (target == null) {
            throw new IllegalArgumentException("Ara Target is null!");
        }

        if (component == null) {
            throw new IllegalArgumentException("Ara Component is null!");
        }

        if (namespace == null) {
            throw new IllegalArgumentException("Ara Namespace is null!");
        }

        if (decodePasswordWithBase64 == null) {
            throw new IllegalArgumentException("Ara decodePasswortWithBase64 is null!");
        }

        final QName qname = new QName("http://schemas.uc4.com/bond/2011-01/DeploymentService", "DeploymentService");
        final URL wsdl = AraPropertyResolver.class.getResource("/DeploymentService.wsdl");

        final DeploymentService_Service serviceFactory = new DeploymentService_Service(wsdl, qname);

        logger.info("ARA Webservice endpoint: {}", araUrl + "/" + SERVICE_NAME);

        final DeploymentService webservice = serviceFactory.getBasicHttpBindingDeploymentService();
        final BindingProvider bp = (BindingProvider) webservice;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, araUrl + "/" + SERVICE_NAME);

        final DeploymentDescriptorResult result = webservice.getDeploymentDescriptor(userName, password, runId);
        if (!result.isIsSuccess()) {
            throw new RuntimeException("Webservice call wasn't successful!\n" + result.getMessage().getValue());
        }

        logger.debug("Webservice response:");
        logger.debug(result.getDeploymentXML().getValue());

        final XBProjector xbProjector = new XBProjector();
        xbProjector.mixins().addProjectionMixin(Variable.class,
                new VariableMixinImpl(getKeyValueSeparatorToken(), getEncoding(), isDecodePasswordWithBase64()));
        xbProjector.mixins().addProjectionMixin(GenerateTask.class, new GenerateTaskMixinImpl(getKeyValueSeparatorToken()));

        setAraData(xbProjector.onXMLString(result.getDeploymentXML().getValue()).createProjection(AraData.class));

        setInitilized(true);
    }

    @Override
    public boolean containsProperty(final String property) {
        initialize();

        final Variable variable = getVariable(property);

        if (variable == null) {
            return false;
        }

        return variable.getValue() != null;
    }

    @Override
    public boolean isProtectedProperty(final String property) {
        initialize();
        logger.trace("Method: IsProtectedProperty [{}]", property);
        return getVariable(property).isEncrypted();
    }

    @Override
    public String getPropertyValue(final String property) {
        initialize();

        logger.trace("Method: getPropertyValue [{}]", property);

        final Variable variable = getVariable(property);
        if (variable == null) {
            return null;
        }

        return variable.getValue();
    }

    private Variable getVariable(final String property) {
        initialize();

        logger.trace("Method: getVariable#start[{}]", property);

        if (!variableCache.containsKey(property)) {
            logger.trace("Method: getVariable#getTask [{}]", property);
            final Task task = getAraData().getTask(component);
            if (task == null) {
                throw new IllegalArgumentException("Couldn't find component [" + component + "]");
            }

            logger.trace("Method: getVariable#getGeneratedTask [{}]", property);
            final GenerateTask generateTask = task.getGenerateTask(target);
            if (generateTask == null) {
                throw new IllegalArgumentException("Couldn't find target [" + target + "]");
            }

            logger.trace("Method: getVariable#getVariable [{}]", property);
            final Variable variable = generateTask.getVariable(namespace, property);
            logger.trace("Method: getVariable#variablePut start [{}]", property);
            variableCache.put(property, variable);
            logger.trace("Method: getVariable#variablePut end [{}]", property);
        }

        return variableCache.get(property);
    }

    @Override
    public Set<String> getPropertyKeys() {
        initialize();

        if (propertyKeys == null) {
            propertyKeys = new TreeSet<String>();

            final Task task = getAraData().getTask(component);
            if (task == null) {
                throw new IllegalArgumentException("Couldn't find component [" + component + "]");
            }

            final GenerateTask generateTask = task.getGenerateTask(target);
            if (generateTask == null) {
                throw new IllegalArgumentException("Couldn't find target [" + target + "]");
            }

            final List<Variable> variables = generateTask.getVariables(namespace);
            for (final Variable variable : variables) {
                if (variable.getValue() != null) {
                    propertyKeys.add(variable.getName());
                }
            }
        }

        return propertyKeys;
    }

    @Override
    public String getEncoding() {
        return "utf-8";
    }

    @Override
    public String getPropertyResolverDescription() {
        return "ARA";
    }

    @Override
    public LinkedHashSet<Defect> checkForDuplicateEntry() {
        return new LinkedHashSet<Defect>();
    }

    @Override
    public String getBeginToken() {
        return beginToken;
    }

    @Override
    public String getEndToken() {
        return endToken;
    }

    public String getAraUrl() {
        return araUrl;
    }

    public void setAraUrl(final String araUrl) {
        this.araUrl = araUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        if (password != null && password.startsWith("--10")) {
            this.password = Maxim.deMaxim(password);
        } else {
            this.password = password;
        }
    }

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(final Integer runId) {
        this.runId = runId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(final String component) {
        this.component = component;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    protected boolean isInitilized() {
        return initilized;
    }

    protected void setInitilized(final boolean initilized) {
        this.initilized = initilized;
    }

    public AraData getAraData() {
        return araData;
    }

    protected void setAraData(final AraData araData) {
        this.araData = araData;
    }

    @Override
    public boolean propertyUsesToken(final String property) {
        logger.trace("Method: propertyUsesToken [{}]", property);
        if (getVariable(property).isEncrypted()) {
            return false;
        }
        return super.propertyUsesToken(property);
    }

    public void setDecodePasswordWithBase64(final Boolean decodePasswordWithBase64) {
        this.decodePasswordWithBase64 = decodePasswordWithBase64;
    }

    public Boolean isDecodePasswordWithBase64() {
        return decodePasswordWithBase64;
    }

    public void setBeginToken(final String beginToken) {
        if (beginToken == null || beginToken.length() == 0) {
            throw new IllegalArgumentException("beginToken can't be null");
        }
        this.beginToken = beginToken;
    }

    public void setEndToken(final String endToken) {
        if (endToken == null || endToken.length() == 0) {
            throw new IllegalArgumentException("endToken can't be null");
        }
        this.endToken = endToken;
    }

    public void setKeyValueSeparatorToken(final String propertyKeyValueSeparator) {
        this.propertyKeyValueSeparator = propertyKeyValueSeparator;
    }

    public String getKeyValueSeparatorToken() {
        return propertyKeyValueSeparator;
    }

}
