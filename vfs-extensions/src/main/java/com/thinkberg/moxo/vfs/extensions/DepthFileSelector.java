package com.thinkberg.moxo.vfs.extensions;

import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;

/**
 * A file selector that operates depth of the directory structure and will
 * select all files up to and including the depth given in the constructor.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class DepthFileSelector implements FileSelector {
  private final int maxDepth;
  private final int minDepth;

  /**
   * Create a file selector that will select ALL files.
   */
  public DepthFileSelector() {
    this(0, Integer.MAX_VALUE);
  }

  /**
   * Create a file selector that will select all files up to and including
   * the directory depth.
   *
   * @param depth the maximum depth
   */
  public DepthFileSelector(int depth) {
    this(0, depth);
  }

  @SuppressWarnings({"SameParameterValue"})
  public DepthFileSelector(int min, int max) {
    minDepth = min;
    maxDepth = max;
  }

  public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
    int depth = fileSelectInfo.getDepth();
    return depth >= minDepth && depth <= maxDepth;
  }

  public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
    return fileSelectInfo.getDepth() < maxDepth;
  }
}
