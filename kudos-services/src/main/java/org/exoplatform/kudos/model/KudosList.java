package org.exoplatform.kudos.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class KudosList implements Serializable {

  private static final long serialVersionUID = 5173858331264945555L;

  private List<Kudos>       kudos;

  private long              limit;

  private long              size;

}
