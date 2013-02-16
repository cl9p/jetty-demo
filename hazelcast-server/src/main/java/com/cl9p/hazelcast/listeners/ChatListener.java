package com.cl9p.hazelcast.listeners;

import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import org.apache.log4j.Logger;

public class ChatListener implements ItemListener {
    Logger logger = Logger.getLogger(ChatListener.class);

    @Override
    public void itemAdded(ItemEvent itemEvent) {
        logger.info("Item added == " + itemEvent.getItem().toString());
    }

    @Override
    public void itemRemoved(ItemEvent itemEvent) {
        logger.info("Item removed == " + itemEvent.getItem().toString());
    }
}
