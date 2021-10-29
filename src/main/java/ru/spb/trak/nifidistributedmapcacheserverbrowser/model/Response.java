package ru.spb.trak.nifidistributedmapcacheserverbrowser.model;

import lombok.Data;

import java.util.Collection;

@Data
public class Response {
    int total;
    Collection<Element> data;
}
