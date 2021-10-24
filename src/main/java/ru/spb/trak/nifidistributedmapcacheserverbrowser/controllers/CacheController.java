package ru.spb.trak.nifidistributedmapcacheserverbrowser.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.ProtocolVersionException;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.Key;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.KeyRepository;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class CacheController {
    private final Logger log = LoggerFactory.getLogger(CacheController.class);
    private KeyRepository keyRepository;

    public CacheController(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @GetMapping("/keys")
    Collection<Key> keys(@RequestParam(name = "host", required = true) String host, @RequestParam(name = "port", required = true) int port) throws UnknownHostException, IOException, ProtocolVersionException {
        return keyRepository.findAll(host, port);
    }

}