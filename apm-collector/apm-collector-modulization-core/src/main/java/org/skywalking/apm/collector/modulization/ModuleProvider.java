/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.modulization;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The <code>ModuleProvider</code> is an implementation of a {@link Module}.
 *
 * And each module can have one or more implementation, which depends on `application.yml`
 *
 * @author wu-sheng
 */
public abstract class ModuleProvider {
    protected ModuleManager manager;
    protected Module module;
    private Map<Class<? extends Service>, Service> services = new HashMap<>();

    public ModuleProvider() {
    }

    void setManager(ModuleManager manager) {
        this.manager = manager;
    }

    void setModule(Module module) {
        this.module = module;
    }

    /**
     * @return the name of this provider.
     */
    public abstract String name();

    /**
     * @return the module name
     */
    public abstract Class<? extends Module> module();

    /**
     * In prepare stage, the module should initialize things which are irrelative other modules.
     *
     * @param config from `application.yml`
     */
    public abstract void prepare(Properties config);

    /**
     * In prepare stage, the module can interop with other modules.
     *
     * @param config from `application.yml`
     */
    public abstract void init(Properties config);

    /**
     * @return module names which does this module require?
     */
    public abstract String[] requiredModules();

    /**
     * Register a implementation for the service of this module provider.
     *
     * @param serviceType
     * @param service
     */
    protected void registerServiceImplementation(Class<? extends Service> serviceType, Service service) {
        this.services.put(serviceType, service);
    }

    /**
     * Make sure all required services have been implemented.
     *
     * @param requiredServices must be implemented by the module.
     * @throws ServiceNotProvidedException when exist unimplemented service.
     */
    void requiredCheck(Class<? extends Service>[] requiredServices) throws ServiceNotProvidedException {
        if (requiredServices == null)
            return;

        if (requiredServices.length != services.size()) {
            throw new ServiceNotProvidedException("Haven't provided enough plugins.");
        }

        for (Class<? extends Service> service : requiredServices) {
            if (!services.containsKey(service)) {
                throw new ServiceNotProvidedException("Service:" + service.getName() + " not provided");
            }
        }
    }
}
