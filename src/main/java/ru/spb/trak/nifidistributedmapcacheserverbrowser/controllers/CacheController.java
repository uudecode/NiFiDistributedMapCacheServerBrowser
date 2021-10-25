package ru.spb.trak.nifidistributedmapcacheserverbrowser.controllers;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.ProtocolVersionException;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.Element;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.ElementRepository;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class CacheController {
    private final Logger log = LoggerFactory.getLogger(CacheController.class);
    private ElementRepository elementRepository;

    public CacheController(ElementRepository elementRepository) {
        this.elementRepository = elementRepository;
    }

    @GetMapping("/keys")
    Collection<Element> keys(@RequestParam(name = "host", required = true) String host,
                             @RequestParam(name = "port", required = true) int port,
                             @RequestParam(name = "pattern", required = false) String pattern) throws UnknownHostException, IOException, ProtocolVersionException {
        if (StringUtils.hasText(pattern))
            return elementRepository.findByPattern(host, port, pattern);
        else
            return elementRepository.findAll(host, port);
    }

}