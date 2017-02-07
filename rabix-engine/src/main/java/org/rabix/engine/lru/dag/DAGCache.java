package org.rabix.engine.lru.dag;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.lru.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DAGCache extends LRUCache<String, DAGNode> {
  
  private final Logger logger = LoggerFactory.getLogger(DAGCache.class);
  
  
  @Inject
  public DAGCache() {
    super("DAGCache");
  }
  
  public DAGCache(int cacheSize) {
    super("DAGCache", cacheSize);
  }
  
  public DAGNode get(String id, String rootId, String dagHash) {
    DAGNode res = null;
    res = get(dagHash);
    if(res != null) {
      logger.debug(String.format("DAGNode rootId=%s, id=%s found in cache", rootId, id));
      logger.debug(String.format("Cache size=%d", size()));
    }
    return res != null ? getIdFromDAG(id, res) : null;    
  }
  
  public String put(DAGNode dagNode, String rootId) {
    String dagHash = hashDagNode(dagNode);
    put(dagHash, dagNode);
    return dagHash;
  }
  
  public DAGNode getIdFromDAG(String id, DAGNode node) {
    Map<String, DAGNode> allNodes = new HashMap<String, DAGNode>();
    populateNodes(node, allNodes);
    return allNodes.get(id);
  }
  
  private void populateNodes(DAGNode node, Map<String, DAGNode> allNodes) {
    allNodes.put(node.getId(), node);
    if (node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        populateNodes(child, allNodes);
      }
    }
  }
  
  public static String hashDagNode(DAGNode dagNode) {
    String dagText = BeanSerializer.serializeFull(dagNode);
    String cachedSortedDAGText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(dagText));
    String cachedDAGHash = ChecksumHelper.checksum(cachedSortedDAGText, HashAlgorithm.SHA1);
    return cachedDAGHash;
  }
  
}
