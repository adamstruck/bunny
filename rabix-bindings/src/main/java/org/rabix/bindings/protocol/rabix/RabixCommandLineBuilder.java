package org.rabix.bindings.protocol.rabix;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.rabix.bean.RabixJobApp;

public class RabixCommandLineBuilder {

  public String buildCommandLine(Job job) throws BindingException {
    RabixJobApp app = (RabixJobApp) RabixAppProcessor.loadAppObject(job.getId(), job.getApp());
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty(Velocity.RESOURCE_LOADER, "string");
    ve.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
    ve.addProperty("string.resource.loader.repository.static", "false");
    ve.init();
    StringResourceRepository repo = (StringResourceRepository) ve
        .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
    repo.putStringResource(app.getId(), app.getTemplate());
    VelocityContext context = new VelocityContext();
    for(String input: job.getInputs().keySet()) {
      context.put(input, job.getInputs().get(input));
    }
    Template template = ve.getTemplate(app.getId());
    StringWriter writer = new StringWriter();
    template.merge(context, writer);
    String commandLine = writer.toString();
    return commandLine;
  }
  
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    RabixJobApp app = (RabixJobApp) RabixAppProcessor.loadAppObject(job.getId(), job.getApp());
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty(Velocity.RESOURCE_LOADER, "string");
    ve.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
    ve.addProperty("string.resource.loader.repository.static", "false");
    ve.init();
    StringResourceRepository repo = (StringResourceRepository) ve
        .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
    repo.putStringResource(app.getId(), app.getTemplate());
    VelocityContext context = new VelocityContext();
    for(String input: job.getInputs().keySet()) {
      context.put(input, job.getInputs().get(input));
    }
    Template template = ve.getTemplate(app.getId());
    StringWriter writer = new StringWriter();
    template.merge(context, writer);
    String commandLine = writer.toString();
    List<String> commandLineParts = new ArrayList<String>();
    String[] split = commandLine.split(" ");
    for(String part: split) {
      commandLineParts.add(part);
    }
    return commandLineParts;
  }
}