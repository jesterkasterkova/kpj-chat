package cz.inventi.kpj.chat.service;

import cz.inventi.kpj.chat.mapper.MessageMapper;
import cz.inventi.kpj.chat.messaging.MessageBroker;
import cz.inventi.kpj.chat.messaging.MessageListener;
import cz.inventi.kpj.chat.model.MessageEvent;
import cz.inventi.kpj.chat.model.MessageType;
import cz.inventi.kpj.openapi.model.Message;
import cz.inventi.kpj.openapi.model.MessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService, MessageListener {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageBroker messageBroker;

    @PostConstruct
    private void init() {
        messageBroker.registerListener(this);
    }

    private final List<Message> messages = new ArrayList<>();

    @Override
    public List<Message> listMessages() {
        return messages;
    }

    @Override
    public UUID createMessage(MessageRequest messageRequest) {
        MessageEvent messageEvent = messageMapper.requestToEvent(messageRequest);
        messageEvent.setType(MessageType.MESSAGE);
        messageEvent.setId(UUID.randomUUID());
        messageEvent.setCreated(OffsetDateTime.now());
        messageBroker.publish(messageEvent);
        // TODO: map messageRequest to messageEvent; set type, id and created; publish messageEvent via messageBroker and return its id
        return messageEvent.getId();
    }

    @Override
    public void onMessage(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case MESSAGE -> messages.add(messageMapper.eventToDTO(messageEvent));
            case PRESENCE -> log.info(messageEvent);
        }
        // TODO: switch over messageEvent type; if it is a message, map it to Message and add it to messages list; if it is a presence check, just
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void sendPresence() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCreated(OffsetDateTime.now());
        messageEvent.setType(MessageType.PRESENCE);
        messageEvent.setId(UUID.randomUUID());
        messageEvent.setName("Nushi");
        messageBroker.publish(messageEvent);
        log.info("Presence message published");
        // TODO: create a new messageEvent with type PRESENCE, id, created and name; publish it via messageBroker and log an info message
    }
}