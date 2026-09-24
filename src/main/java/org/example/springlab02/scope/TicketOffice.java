package org.example.springlab02.scope;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TicketOffice {

    private final Ticket direct;
    private final ObjectProvider<Ticket> provider;

    public TicketOffice(Ticket direct, ObjectProvider<Ticket> provider) {
        this.direct = direct;
        this.provider = provider;
    }

    public Map<String, Object> demo() {
        Ticket first = provider.getObject();
        Ticket second = provider.getObject();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("injectedDirectly", direct.id());
        result.put("viaProviderFirst", first.id());
        result.put("viaProviderSecond", second.id());
        result.put("office", System.identityHashCode(this));
        return result;
    }
}