package org.rabix.engine.rest.backend.stub.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.activemq.TransportPluginActiveMQ;
import org.rabix.transport.mechanism.impl.activemq.TransportQueueActiveMQ;

public class BackendStubActiveMQ extends BackendStub<TransportQueueActiveMQ, BackendActiveMQ, TransportPluginActiveMQ> {

  public BackendStubActiveMQ(JobService jobService, Configuration configuration, BackendActiveMQ backend) throws TransportPluginException {
    this.backend = backend;
    this.jobService = jobService;
    this.transportPlugin = new TransportPluginActiveMQ(configuration);

    this.sendToBackendQueue = new TransportQueueActiveMQ(backend.getToBackendQueue());
    this.sendToBackendControlQueue = new TransportQueueActiveMQ(backend.getToBackendControlQueue());
    this.receiveFromBackendQueue = new TransportQueueActiveMQ(backend.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueActiveMQ(backend.getFromBackendHeartbeatQueue());
  }

}
