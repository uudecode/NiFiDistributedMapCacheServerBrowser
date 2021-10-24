package ru.spb.trak.nifidistributedmapcacheserverbrowser.model;


import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@ToString
public class Key {
  private final String key;
  private JSONPObject value;
}
