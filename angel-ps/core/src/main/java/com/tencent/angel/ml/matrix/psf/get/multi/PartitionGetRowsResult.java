package com.tencent.angel.ml.matrix.psf.get.multi;

import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.protobuf.generated.MLProtos;
import com.tencent.angel.ps.impl.matrix.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of get row function for a matrix partition.
 */
public class PartitionGetRowsResult extends PartitionGetResult {
  /** row splits */
  private List<ServerRow> rowSplits;

  /**
   * Create a new PartitionGetRowsResult.
   *
   * @param rowSplits row splits
   */
  public PartitionGetRowsResult(List<ServerRow> rowSplits) {
    this.rowSplits = rowSplits;
  }

  /**
   * Create a new PartitionGetRowsResult.
   */
  public PartitionGetRowsResult() {
    this(null);
  }

  @Override
  public void serialize(ByteBuf buf) {
    if (rowSplits == null) {
      buf.writeInt(0);
    } else {
      int size = rowSplits.size();
      buf.writeInt(size);
      for (int i = 0; i < size; i++) {
        buf.writeInt(rowSplits.get(i).getRowType().getNumber());
        rowSplits.get(i).serialize(buf);
      }
    }
  }

  @Override
  public void deserialize(ByteBuf buf) {
    int size = buf.readInt();
    rowSplits = new ArrayList<ServerRow>(size);
    for (int i = 0; i < size; i++) {
      MLProtos.RowType type = MLProtos.RowType.valueOf(buf.readInt());
      ServerRow rowSplit = null;
      switch (type) {
        case T_DOUBLE_DENSE: {
          rowSplit = new ServerDenseDoubleRow();
          break;
        }

        case T_DOUBLE_SPARSE: {
          rowSplit = new ServerSparseDoubleRow();
          break;
        }

        case T_INT_DENSE: {
          rowSplit = new ServerDenseIntRow();
          break;
        }

        case T_INT_SPARSE: {
          rowSplit = new ServerSparseIntRow();
          break;
        }

        case T_FLOAT_DENSE: {
          rowSplit = new ServerDenseFloatRow();
          break;
        }

        default:
          break;
      }

      rowSplit.deserialize(buf);
      rowSplits.add(rowSplit);
    }
  }

  @Override
  public int bufferLen() {
    int len = 0;
    if (rowSplits != null) {
      int size = rowSplits.size();
      for (int i = 0; i < size; i++) {
        len += rowSplits.get(i).bufferLen();
      }
    }

    return len;
  }

  /**
   * Get row splits.
   *
   * @return List<ServerRow> row splits
   */
  public List<ServerRow> getRowSplits() {
    return rowSplits;
  }
}