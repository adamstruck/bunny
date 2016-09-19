package org.rabix.bindings.cwl.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorCallback;
import org.rabix.bindings.cwl.processor.CWLPortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class CWLFileValueFlattenProcessorCallback implements CWLPortProcessorCallback {

  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;

  protected CWLFileValueFlattenProcessorCallback(Set<String> visiblePorts) {
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
  }

  @Override
  public CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    // it's a File
    if (CWLSchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      fileValues.add(CWLFileValueHelper.createFileValue(value));
      
      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          fileValues.add(CWLFileValueHelper.createFileValue(secondaryFileValue));
        }
      }
      return new CWLPortProcessorResult(value, true);
    }
    // it's a Directory
    if (CWLSchemaHelper.isDirectoryFromValue(value) && !skip(port.getId())) {
      
    }
    return new CWLPortProcessorResult(value, false);
  }
  
  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(CWLSchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
