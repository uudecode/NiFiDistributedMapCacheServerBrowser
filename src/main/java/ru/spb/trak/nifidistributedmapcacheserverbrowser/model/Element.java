package ru.spb.trak.nifidistributedmapcacheserverbrowser.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
  public class Element {
  private final String key;
  private String value;
}
