package com.github.bingoohuang.springrestclient.demo;

import com.github.bingoohuang.springrestclient.boot.domain.PayParty;
import com.github.bingoohuang.springrestclient.spring.PayPartyApi;
import com.github.bingoohuang.springrestclient.utils.UniRestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;

public class PayPartyApiDemo implements PayPartyApi {
    @Override
    public PayParty party(@PathVariable("sellerId") String sellerId, @PathVariable("buyerId") String buyerId, @RequestParam("partyId") String partyId, @RequestParam("name") String name) {
        return null;
    }

    @Override
    public int addPary(@RequestBody PayParty payParty) {
        LinkedHashMap pathVariables = new LinkedHashMap();
        LinkedHashMap requestParams = new LinkedHashMap();

        String str = UniRestUtils.asPrimitive("url", pathVariables, requestParams, payParty);

        return Integer.valueOf(str);
    }
}