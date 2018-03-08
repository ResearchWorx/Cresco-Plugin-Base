package com.researchworx.cresco.library.plugin;

import com.researchworx.cresco.library.messaging.CAddr;
import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.plugin.core.CPlugin;

public class TestPlugin extends CPlugin{
    protected void start() {
        MsgEvent msg = createMsgEvent(MsgEvent.Type.INFO, new CAddr("some_region", "some_agent", "some_plugin"));
    }
}
