package org.rabix.bindings.protocol.draft2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.RequirementProvider;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.requirement.CPURequirement;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.MemoryRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.resource.Draft2CpuResource;
import org.rabix.bindings.protocol.draft2.bean.resource.Draft2MemoryResource;
import org.rabix.bindings.protocol.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2DockerResource;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2EnvVarRequirement;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2EnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.protocol.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.protocol.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolJobHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2SchemaHelper;

public class Draft2RequirementProvider implements RequirementProvider {

  @Override
  public Job populateResources(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);
    try {
      Resources resources = new Resources(draft2Job.getCPU(), draft2Job.getMemory(), null, null);   // TODO populate
      return Job.cloneWithResources(job, resources);
    } catch (Draft2ExpressionException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public DockerContainerRequirement getDockerRequirement(Job job) throws BindingException {
    Draft2JobApp draft2JobApp = new Draft2ProtocolJobHelper().getJob(job).getApp();
    Draft2DockerResource draft2DockerResource = draft2JobApp.getContainerResource();
    return getDockerRequirement(draft2DockerResource);
  }
  
  private DockerContainerRequirement getDockerRequirement(Draft2DockerResource draft2DockerResource) {
    if (draft2DockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(draft2DockerResource.getDockerPull(), draft2DockerResource.getImageId());
  }

  @Override
  public EnvironmentVariableRequirement getEnvironmentVariableRequirement(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);

    Draft2EnvVarRequirement envVarRequirement = draft2Job.getApp().getEnvVarRequirement();
    return getEnvironmentVariableRequirement(draft2Job, envVarRequirement);
  }
  
  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(Draft2Job draft2Job, Draft2EnvVarRequirement envVarRequirement) throws BindingException {
    if (envVarRequirement == null) {
      return null;
    }

    List<EnvironmentDef> envDefinitions = envVarRequirement.getEnvironmentDefinitions();
    if (envDefinitions == null) {
      return new EnvironmentVariableRequirement(Collections.<String> emptyList());
    }
    List<String> result = new ArrayList<>();
    for (EnvironmentDef envDef : envDefinitions) {
      String key = envDef.getName();
      Object value = envDef.getValue();

      if (Draft2ExpressionBeanHelper.isExpression(value)) {
        try {
          value = Draft2ExpressionBeanHelper.evaluate(draft2Job, value);
        } catch (Draft2ExpressionException e) {
          throw new BindingException(e);
        }
      }
      if (value == null) {
        throw new BindingException("Environment variable for " + key + " is empty.");
      }
      result.add(key + "=" + value);
    }
    return new EnvironmentVariableRequirement(result);
  }

  @Override
  public FileRequirement getFileRequirement(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);

    Draft2CreateFileRequirement createFileRequirement = draft2Job.getApp().getCreateFileRequirement();
    return getFileRequirement(draft2Job, createFileRequirement);
  }
  
  private FileRequirement getFileRequirement(Draft2Job draft2Job, Draft2CreateFileRequirement createFileRequirement) throws BindingException {
    if (createFileRequirement == null) {
      return null;
    }

    List<Draft2CreateFileRequirement.Draft2FileRequirement> fileRequirements = createFileRequirement.getFileRequirements();
    if (fileRequirements == null) {
      return null;
    }

    List<SingleFileRequirement> result = new ArrayList<>();
    for (Draft2CreateFileRequirement.Draft2FileRequirement fileRequirement : fileRequirements) {
      try {
        String filename = (String) fileRequirement.getFilename(draft2Job);

        Object content = fileRequirement.getContent(draft2Job);

        if (Draft2SchemaHelper.isFileFromValue(content)) {
          FileValue fileValue = Draft2FileValueHelper.createFileValue(content);
          result.add(new FileRequirement.SingleInputFileRequirement(filename, fileValue));
        } else {
          result.add(new FileRequirement.SingleTextFileRequirement(filename, (String) content));
        }
      } catch (Draft2ExpressionException e) {
        throw new BindingException(e);
      }
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);
    Draft2JobApp draft2JobApp = draft2Job.getApp();
    return convertRequirements(job, draft2JobApp.getRequirements());
  }
  
  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);
    Draft2JobApp draft2JobApp = draft2Job.getApp();
    return convertRequirements(job, draft2JobApp.getHints());
  }
  
  private List<Requirement> convertRequirements(Job job, List<Draft2Resource> resources) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getJob(job);

    List<Requirement> result = new ArrayList<>();
    try {
      for (Draft2Resource draft2Resource : resources) {
        if (draft2Resource instanceof Draft2CpuResource) {
          Draft2CpuResource draft2CPUResource = (Draft2CpuResource) draft2Resource;
          Integer cpu = draft2CPUResource.getCpu(draft2Job);
          result.add(new CPURequirement(cpu));
          continue;
        }
        if (draft2Resource instanceof Draft2MemoryResource) {
          Draft2MemoryResource draft2MemoryResource = (Draft2MemoryResource) draft2Resource;
          Integer memory = draft2MemoryResource.getMemory(draft2Job);
          result.add(new MemoryRequirement(memory));
          continue;
        }
        if (draft2Resource instanceof Draft2DockerResource) {
          result.add(getDockerRequirement((Draft2DockerResource) draft2Resource));
          continue;
        }
        if (draft2Resource instanceof Draft2EnvVarRequirement) {
          result.add(getEnvironmentVariableRequirement(draft2Job, (Draft2EnvVarRequirement) draft2Resource));
          continue;
        }
        if (draft2Resource instanceof Draft2CreateFileRequirement) {
          result.add(getFileRequirement(draft2Job, (Draft2CreateFileRequirement) draft2Resource));
          continue;
        }
      }
    } catch (Draft2ExpressionException e) {
      throw new BindingException(e);
    }
    return result;
  }

}
